/*
 * main.c FUSE SET ON FF D9 !!!!
 *
 *  Created on: 4 lip 2016
 *      Author: admin
 */
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>


#include "MK_USART/mkuart.h"
#include "soft_timers.h"


#include "common.h"
#include "MK_GSM/mk_gsm.h"
#include "MK_GSM/mk_atcmdlist.h"



char buf[UART_RX_BUF_SIZE];
uint8_t c=0,d=0;
void POST(void);
void tmr_at_process( TSTIMER * tmr );
void gsm_soft_start( void );
void parse_rs232( char * abuf );




int main( void ) {
	ENGINE_OFF;
	LIGHT_OFF;
	TRUNK_OFF;
	LIMIT_OFF;
	PORTD|=(1<<PD2)|(1<<PD3);
	beginning:
	PWR_KEY_DIR	 |= TRUNK|LIGHT|ENGINE|SUPPLY|LIMIT|ALARM;
	SUPPLY_ON;
	alarm='0';
	engine='0';
	gpsprocess.eng_st = _on;
	Lattitude=",,,";
	Longitude="0";
	bat=1000;
	battery="1000";

	//****************** inicjalizacja timerÛw programowych
	soft_timers_init();
	adc_init();
	timer_init( _tmr_at_process, AT_PROCESS_TIMER_TICK, _disable, tmr_at_process );


	//****************** inicjalizacja RS232
	sei();
	USART_Init( 95 );
	register_uart_str_rx_event_callback( parse_rs232 );
	gsm_soft_start();

	while(1) {
		if(gpsprocess.status==_restart){
				goto beginning;
			}
		UART_RX_STR_EVENT( buf );
		POST();
		TIMERS_EVENT( -1 );
	}
}


void tmr_at_process( TSTIMER * tmr ) {

	if( atprocess.status < _at_idle ) {
		// zatrzymanie timera - zmienna atprocess.status zawiera
		// kod operacji: _at_ok lub jeden z kodÛw b≥Ídu
		tmr->enabled = _disable;
		return;
	}
	else if( _at_idle == atprocess.status ) {
		// tu realizujemu TIMEOUT dla komend AT
		if( atprocess.tmo_cnt++ > atprocess.timeout ) {
			atprocess.status = _at_timeout;
			tmr->enabled = _disable;
		}
		return;
	}

	atprocess.status--;
	atprocess.tmo_cnt = 0;
}



void gsm_soft_start( void ) {

		gpsprocess.status=_search;
		gpsprocess.tmo_cnt=0;
		mDelay(1000);
		SUPPLY_OFF;
		c=0;
		mDelay(11000);
		do {c++;
			send_at( _ate, NULL, NULL, 200 );
			res = wait_for_at_process_end();
			if( res<0 && c>10) {
				gpsprocess.status=_restart;
				break;
			}
		} while( res !=0 );
		c=0;
		if(gpsprocess.status==_restart){
			return;
		}
		do {c++;
			send_at( _gps, NULL, NULL, 200 );
			res = wait_for_at_process_end();
			if( res<0 && c>5) {
				gpsprocess.status=_restart;
				break;
			}
		} while( res !=0 );
		c=0;
		if(gpsprocess.status==_restart){
			return;
		}
		do {c++;
			send_at( _cstt, NULL, NULL, 200 );
			res = wait_for_at_process_end();
			if( res<0 && c>2) {
				gpsprocess.status=_restart;
				break;
			}
		} while( res !=0 );
		c=0;
		if(gpsprocess.status==_restart){
			return;
		}
		do {c++;
			send_at( _ciicr, NULL, NULL, 6000 );
			res = wait_for_at_process_end();
			if( res<0  && c>2) {
			gpsprocess.status=_restart;
			break;
			}
		} while( res !=0 );
		c=0;
		if(gpsprocess.status==_restart){
			return;
		}
		uart_puts_P( (char*)pgm_read_word( &atlist[ _at ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _creg ] ) );
		uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
		mDelay(200);
		gpsprocess.status=_post;
		do {c++;
				send_at( _cipstart, NULL, NULL, 3000 );
				res = wait_for_at_process_end();
				if( res<0 && c>3 ) {
					gpsprocess.status=_restart;
					break;
				}
			} while( res !=0 );
		c=0;
		if(gpsprocess.status==_restart){
			return;
		}
		gpsprocess.status=_search;

}




//**********************************************************************************
// W≥asny parser danych RS232
//**********************************************************************************
void parse_rs232( char * abuf ) {


	// sprawdzamy odpowiedü modemu na komendy AT czy OK czy ERROR
	if( (atprocess.status)&&(gpsprocess.status!=_post) ) {
		if( !strcmp_P(abuf, PSTR("OK")) ){
			atprocess.status = _at_ok;
		} else
		if( !strcmp_P(abuf, PSTR("ERROR")) ) {
			atprocess.status = _at_error;
		}
	}

	if((gpsprocess.status==_void)&&(strstr(abuf, "+CGNSINF")!= NULL) ) {

			cli();
			Lattitude = buf;
			sei();
			atprocess.status = _at_ok;
			gpsprocess.status=_search;
		}

	if (gpsprocess.status ==_post){
		if( !strcmp_P(abuf, PSTR("CONNECT OK")) ){
				atprocess.status = _at_ok;
			}
		}

	if(strstr_P(abuf, PSTR ("*fIn&"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_OFF;
			LIGHT_OFF;
			gpsprocess.eng_st = _off;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*rE&"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_OFF;
			LIGHT_OFF;
			gpsprocess.eng_st = _off;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*pA&"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_OFF;
			LIGHT_OFF;
			gpsprocess.eng_st = _on;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*oF&"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_OFF;
			LIGHT_OFF;
			gpsprocess.eng_st = _off;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*oN&"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_ON;
			LIGHT_ON;
			gpsprocess.eng_st = _on;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("$oNn%"))!= NULL) {
			atprocess.status = _at_ok;
			ENGINE_ON;
			LIGHT_ON;
			gpsprocess.eng_st = _on;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*hE&"))!= NULL) {
			atprocess.status = _at_ok;
			TRUNK_ON;
			mDelay(500);
			TRUNK_OFF;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*lIm&"))!= NULL) {
			atprocess.status = _at_ok;
			LIMIT_ON;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*lIoF&"))!= NULL) {
			atprocess.status = _at_ok;
			LIMIT_OFF;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*aLoN&"))!= NULL) {
			atprocess.status = _at_ok;
			ALARM_ON;
			gpsprocess.status = _search;
			abuf="";
		}
	if(strstr_P(abuf, PSTR("*aLoF&"))!= NULL) {
			atprocess.status = _at_ok;
			ALARM_OFF;
			gpsprocess.status = _search;
			abuf="";
		}
}

void POST (void){

	if (gpsprocess.status == _search) {


				c=0;
				if(gpsprocess.tmo_cnt!=0){
					gpsprocess.status = _post;
					do {c++;
							wait_for_site(1000);
							res = wait_for_site_process_end();
							if(res<0) {
								break;
							}
						} while( res !=0 );
					c=0;
				}
				gpsprocess.status = _post;
				do {c++;
						send_at( _cipsend, NULL, NULL, 3000 );
						res = wait_for_at_process_end();

						if(res<0 && c>2){
							uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
							uart_puts_P( (char*)pgm_read_word( &atlist[ _enter] ) );
							uart_putc(0x1A);
							mDelay(700);
							uart_puts_P( (char*)pgm_read_word( &atlist[ _at ] ) );
							uart_puts_P( (char*)pgm_read_word( &atlist[ _cipclose ] ) );
							uart_puts_P( (char*)pgm_read_word( &atlist[ _enter ] ) );
							mDelay(200);
							gpsprocess.status=_reconnect;
							break;
						}
					} while( res !=0 );
				c=0;
				if (gpsprocess.status==_reconnect){
					return;
				}
				if (gpsprocess.status==_restart){
					return;
				}
				Lattitude=",,,";
				gpsprocess.tmo_cnt++;
				gpsprocess.status=_search;
				if(gpsprocess.tmo_cnt==10){
					bat=adc_read();
				}
				if(gpsprocess.tmo_cnt==15){
					itoa(bat,battery,10);
				}
				if(gpsprocess.tmo_cnt==20){
					gpsprocess.status=_void;
					gpsprocess.tmo_cnt=0;
					mDelay(200);
					do {c++;
							send_at( _gpsrd, NULL, NULL, 1000 );
							res = wait_for_at_process_end();
							if( res<0 && c>1 ) {
								gpsprocess.status=_search;
								break;
							}
						} while( res !=0 );
				}
	}
	if (gpsprocess.status == _gprmc) {

	}

	if(gpsprocess.status==_reconnect) {
		c=0;
		gpsprocess.status=_post;
		do {c++;
				send_at( _cipstart, NULL, NULL, 6000 );
				res = wait_for_at_process_end();
				Lattitude=",,,";
				if( res<0 && c>1 ) {
					gpsprocess.status=_restart;
					break;
				}
			} while( res !=0 );
		if(gpsprocess.status!=_restart)gpsprocess.status=_search;
		c=0;
		}
	else {}


}





