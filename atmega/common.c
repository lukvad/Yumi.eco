/*
 * common.c
 *
 *  Created on: 6 lip 2016
 *      Author: admin
 */
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>


#include "MK_USART/mkuart.h"
#include "soft_timers.h"


#include "MK_GSM/mk_gsm.h"


#include "common.h"
#include "MK_GSM/mk_atcmdlist.h"
uint8_t er_cnt=0;

void adc_init(void){
	ADMUX |= (1 << REFS0);
	ADCSRA |= (1 << ADPS2) | (1 << ADPS1) | (1 << ADPS0);
	ADCSRA |= (1 << ADEN);
}

uint16_t adc_read(){
ADCSRA |= (1 << ADSC);
while(ADCSRA & (1<<ADSC));
return ADC;
}


void MAIN_EVENTS( int8_t tmr_nr ) {
	UART_RX_STR_EVENT( buf );
	TIMERS_EVENT( tmr_nr );
}


void mDelay( uint16_t ms ) {
	while( ms-- ) {
		_delay_ms(1);
	}
}





