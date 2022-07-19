/*
 * mk_gsm.c
 *
 *  Created on: 6 lip 2016
 *      Author: admin
 */
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>


#include "../MK_USART/mkuart.h"
#include "../soft_timers.h"
#include "../common.h"

#include "mk_gsm.h"
#include "mk_atcmdlist.h"


TATPROCESS atprocess;
GPSPROCESS gpsprocess;

uint8_t send_at( uint8_t at, const char * pgm_par, char * ram_par, uint16_t tmo ) {

	atprocess.atcmd = at;

	if(atprocess.atcmd != _host){
		uart_puts_P( (char*)pgm_read_word( &atlist[_at] ) );
	}
	if( atprocess.atcmd > 0 ) uart_puts_P( (char*)pgm_read_word( &atlist[ atprocess.atcmd ] ) );

	if( pgm_par ) uart_puts_P( pgm_par );
	if( ram_par ) uart_puts( ram_par );

	uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
	if(at==_cipsend){
		mDelay(200);
		uart_puts_P( (char*)pgm_read_word( &atlist[ _get_token ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _get_slash ] ) );
		uart_puts(Lattitude);
		uart_puts_P( (char*)pgm_read_word( &atlist[ _get_slash ] ) );
		uart_puts(battery);
		uart_puts_P( (char*)pgm_read_word( &atlist[ _get_slash ] ) );
		uart_putc(alarm);
		uart_puts_P( (char*)pgm_read_word( &atlist[ _get_slash ] ) );
		uart_putc(engine);
		uart_puts_P( (char*)pgm_read_word( &atlist[ _http ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _host ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
		uart_putc(0x1A);
	}

	atprocess.timeout = tmo/AT_PROCESS_TIMER_TICK;
	atprocess.status = _at_start;
	timer_enable( _tmr_at_process );
	return 0;

}
uint8_t wait_for_site(uint16_t tmo ) {
	atprocess.timeout = tmo/AT_PROCESS_TIMER_TICK;
	atprocess.status = _at_start;
	timer_enable( _tmr_at_process );
	return 0;
}
int8_t wait_for_site_process_end( void ) {
	if(gpsprocess.eng_st==_off){
		engine='0';
		if((bit_is_set(PIND,PD2))||(bit_is_set(PIND,PD3))){
			alarm='1';
		}
		if((bit_is_clear(PIND,PD2))&&(bit_is_clear(PIND,PD3))){
			alarm='0';
		}
	}
	if(gpsprocess.eng_st==_on){
		engine='1';
	}
	while( atprocess.status > _at_ok ) {
		MAIN_EVENTS( _tmr_at_process );
	}
	return atprocess.status;
}


int8_t wait_for_at_process_end( void ) {

	while( atprocess.status > _at_ok ) {
		MAIN_EVENTS( _tmr_at_process );
	}
	return atprocess.status;
}
