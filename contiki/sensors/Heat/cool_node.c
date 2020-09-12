#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "os/dev/leds.h"
#include "os/dev/button-hal.h"

// Log configuration
#include "sys/log.h"
#define LOG_MODULE "Cool_node"
#define LOG_LEVEL LOG_LEVEL_INFO

// Resources
extern coap_resource_t res_temperature; // sensor
extern coap_resource_t res_cooler; // actuator

extern enum signals { GREEN, YELLOW, RED } warn_level;
extern bool forced_cooling;
int sensor_period = 2;

#define SERVER_EP ("coap://[fd00::1]:5683")
PROCESS(cool_process, "Cool_node");
AUTOSTART_PROCESSES(&cool_process);

// ignore response
void client_chunk_handler(coap_message_t *response){
	if(response == NULL) {
	  return;
	}
}


PROCESS_THREAD(cool_process, ev, data){
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
   	static struct etimer timer;
	PROCESS_BEGIN();
	
	//acivate the resources
	coap_activate_resource(&res_temperature, "sensors/temperature");
	coap_activate_resource(&res_cooler, "actuators/cooler");
	
	//pupolate coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

	//prepare the message
	coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
	coap_set_header_uri_path(request, "registrant");
	LOG_INFO("registering\n");
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	
	LOG_INFO("registered\n");
	etimer_set(&timer, CLOCK_SECOND * sensor_period);

	while(true) {
		
		PROCESS_WAIT_EVENT();
		if(ev == PROCESS_EVENT_TIMER && data == &timer){
			res_temperature.trigger();
			if (warn_level == GREEN) {
				leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
			} else if (warn_level == YELLOW) {
				leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
			} else if (warn_level == RED) {
				leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
			}
			
			etimer_set(&timer, CLOCK_SECOND * sensor_period);

		} else if (ev == button_hal_press_event) {
			forced_cooling = true;
			LOG_INFO("cooling button pressed\n");
		} else if (ev== button_hal_release_event) {
			forced_cooling = false;
			LOG_INFO("cooling button released\n");
		}
	}
	
	PROCESS_END();
}
 





