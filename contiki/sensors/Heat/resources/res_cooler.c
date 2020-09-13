#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"

//Log configuration
#include "sys/log.h"
#define LOG_MODULE "Temperature sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

extern double temperature;
extern bool cooling;
bool forced_cooling = false;
double max_temp = 35;
extern int cycles_of_active_cooling;
extern int sensor_period;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_cooler,
	 "title=\"Air cooler\";methods=\"GET/POST/PUT\"max_temp=double\";rt=\"actuator\"\n",
	 res_get_handler,
	 res_post_put_handler,
	 res_post_put_handler,
	 NULL);

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	if (request != NULL) {
		LOG_DBG("received POST/PUT \n");
	}
	int success = 1;
	size_t len_mode;
	const char *tmp_value;
	if ((len_mode = coap_get_post_variable(request, "max_temp", &tmp_value))){
		double tmp_double;
		char* tmp_ptr;
		tmp_double = strtod(tmp_value, &tmp_ptr);
		/* If the result is 0, test for an error */
		if (tmp_double == 0 && errno == ERANGE) {
			LOG_INFO("Incorrect parameter\n");
			success = 0;
		} else {
			max_temp = tmp_double;
		}
		LOG_INFO("max_temp %lf\n", max_temp);
	} else {
		success = 0;
	}

	if (!success) {
		coap_set_status_code(response, BAD_REQUEST_4_00);
	} else {
		coap_set_status_code(response, CHANGED_2_04);
	}
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	unsigned int accept = APPLICATION_JSON;
	coap_get_header_accept(request, &accept);
	
	if (accept == TEXT_PLAIN) {
		coap_set_header_content_format(response, TEXT_PLAIN);
  		coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "mt: %lf, c: %d, t: %lf, as: %d \n", max_temp, cooling ? 1 : 0, temperature, cycles_of_active_cooling * sensor_period));
	} else if (accept == APPLICATION_XML) {
		coap_set_header_content_format(response, APPLICATION_XML);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<cR><mt=\"%lf\"/><c=\"%d\"/><t=\"%lf\"/><as=\"%d\"/></cR>", max_temp, cooling ? 1 : 0, temperature, cycles_of_active_cooling * sensor_period);
		coap_set_payload(response, buffer, strlen((char *)buffer));
  	} else if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"mt\":%lf, \"c\":%d, \"t\":%lf, \"as\":%d }", max_temp, cooling ? 1 : 0, temperature, cycles_of_active_cooling * sensor_period);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    }
}


