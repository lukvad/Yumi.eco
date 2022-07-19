/*                  e-gadget.header
 * soft_timers.h
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

#ifndef SOFT_TIMERS_H_
#define SOFT_TIMERS_H_


#define MAX_TIMERS 	3


enum { _disable, _enable };


typedef struct TSTIMER {
	volatile uint16_t cnt;
	uint8_t enabled;
	uint16_t interval;
	void (* tmr_callback)( struct TSTIMER * tmr );
} TSTIMER;


extern TSTIMER stimers[];


//void tmr_irq_tick( void ) __attribute__((always_inline));
inline void tmr_irq_tick( void ) {
	uint16_t n;
	for( uint8_t i=0; i<MAX_TIMERS; i++ ) {
		n = stimers[i].cnt;
		if (n) stimers[i].cnt = --n;
	}
}


void soft_timers_init( void );

void TIMERS_EVENT( int8_t tmr_nr );

void timer_init( uint8_t nr, uint16_t interval, uint8_t enabled, void (*callback)( struct TSTIMER * tmr ) );
void timer_enable( uint8_t nr );
uint8_t timer_isenabled( uint8_t nr );
void timer_disable( uint8_t nr );
void timer_interval( uint8_t nr, uint16_t interval );




#endif /* SOFT_TIMERS_H_ */


