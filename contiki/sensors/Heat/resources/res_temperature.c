#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"

// Log configuration
#include "sys/log.h"
#define LOG_MODULE "Temperature sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

double temperature = 30;
bool cooling = false;
int cycles_of_active_cooling = 0; //number of time observing function is triggered

enum signals { GREEN, YELLOW, RED };
unsigned long timest;
extern bool forced_cooling;
extern double max_temp;
enum signals warn_level = GREEN;
extern int sensor_period;

EVENT_RESOURCE(res_temperature,
		       "title=\"Temperature sensor\";methods=\"GET\";rt=\"sensor\";obs\n",
		        res_get_handler,
		        NULL,
		        NULL,
				NULL,
		        res_event_handler);

static void res_event_handler(void) {
	if (temperature > max_temp || forced_cooling) {
		temperature -= ((double)rand() / RAND_MAX) * 2 + 0.1;
		cooling = true;
		cycles_of_active_cooling = cycles_of_active_cooling + 1;
	} else {
		temperature += ((double)rand() / RAND_MAX) * 5 + 0.1;
		cooling = false;
		cycles_of_active_cooling = 0;
	}

	if (temperature - max_temp > max_temp * 0.1) {
		warn_level = RED;
		LOG_DBG("ROSSO\n");
	} else if (temperature > max_temp) {
		warn_level = YELLOW;
		LOG_DBG("GIALLO\n");
	} else {
		warn_level = GREEN;
		LOG_DBG("VERDE\n");
	}
	coap_notify_observers(&res_temperature);
	LOG_DBG("%lf \n", temperature);
	LOG_DBG("%d \n", cycles_of_active_cooling);

}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	unsigned int accept = APPLICATION_JSON;
	coap_get_header_accept(request, &accept);
	
	timest = (unsigned long)time(NULL);
		
	if (accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"t\":%lf,\"dt\":%lu, \"mt\":%lf}", temperature, timest, max_temp); //, sensor_period * cycles_of_active_cooling);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-types application/json";
		coap_set_payload(response, msg, strlen(msg));
	}
    
}



















