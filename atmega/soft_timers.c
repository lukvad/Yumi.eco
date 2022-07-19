/*                  e-gadget.header
 * soft_timers.c
 *
 *  Created on: 2016-06-21
 *    Modyfied: 2016-06-21 00:23:51
 *      Author: Miros³aw Kardaœ
 *
 *	www.atnel.pl
 *	ATNEL YELLOWBOOK
 *
 *
 */
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>
#include <stdlib.h>
#include <string.h>


#include "soft_timers.h"


TSTIMER stimers[ MAX_TIMERS ];

void soft_timers_init( void ) {


	// ustawienie TIMER2
	TCCR2 |= (1<<WGM21);						// tryb CTC
	TCCR2 |= (1<<CS22)|(1<<CS21)|(1<<CS20);		// preskaler = 256
	OCR2 = (F_CPU/1024UL/100UL);				// odœwie¿anie 100 Hz
	TIMSK |= (1<<OCIE2);						// zezwolenie na przerwanie CompareMatch

}

ISR( TIMER2_COMP_vect ) {
	tmr_irq_tick();
}


static void timers_process( TSTIMER * tmr ) {

	if( !tmr->cnt ) {

		if( tmr->tmr_callback ) tmr->tmr_callback( tmr );

		if( tmr->interval < 10 ) tmr->interval = 10;
		tmr->cnt = tmr->interval/10;

	}
}


void timer_init( uint8_t nr, uint16_t interval, uint8_t enabled, void (*callback)( struct TSTIMER * tmr ) ) {
	stimers[nr].interval = interval;
	stimers[nr].tmr_callback = callback;
	stimers[nr].cnt = interval/10;
	stimers[nr].enabled = enabled;
}

void timer_enable( uint8_t nr ) {
	uint8_t sreg = SREG;
	cli();
	stimers[nr].enabled = 0;
	stimers[nr].cnt = stimers[nr].interval/10;
	stimers[nr].enabled = 1;
	SREG = sreg;
}

uint8_t timer_isenabled( uint8_t nr ) {
	return stimers[nr].enabled;
}

void timer_disable( uint8_t nr ) {
	stimers[nr].enabled = 0;
}

void timer_interval( uint8_t nr, uint16_t interval ) {
	uint8_t sreg = SREG;
	cli();
	stimers[nr].interval = interval;
	stimers[nr].cnt = interval/10;
	SREG = sreg;
}

void TIMERS_EVENT( int8_t tmr_nr ) {

	if( tmr_nr < 0 ) {
		for( uint8_t i=0; i<MAX_TIMERS; i++ ) {
			if( stimers[ i ].enabled ) timers_process( &stimers[ i ] );
		}
	} else {
		if( stimers[ 0 ].enabled ) timers_process( &stimers[ 0 ] );
		if( tmr_nr && stimers[ tmr_nr ].enabled ) timers_process( &stimers[ tmr_nr ] );
	}
}




