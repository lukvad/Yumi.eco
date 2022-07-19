/*
 * mk_gsm.h
 *
 *  Created on: 6 lip 2016
 *      Author: admin
 */

#ifndef MK_GSM_MK_GSM_H_
#define MK_GSM_MK_GSM_H_



#define AT_PROCESS_TIMER_TICK	100


//-------------- sygna³ PWR_KEY -----------------
#define LIGHT		(1<<PB1)
#define PWR_KEY_DIR		DDRB
#define PWR_KEY_PORT	PORTB

#define LIGHT_ON	    PWR_KEY_PORT |= LIGHT
#define LIGHT_OFF		PWR_KEY_PORT &= ~LIGHT
//-------------- sygna³ DIODY -----------------
#define ENGINE			(1<<PB3)

#define ENGINE_OFF		PWR_KEY_PORT &= ~ENGINE
#define ENGINE_ON		PWR_KEY_PORT |= ENGINE
//-------------- sygna³ DIODY -----------------
#define TRUNK			(1<<PB5)

#define TRUNK_OFF		PWR_KEY_PORT &= ~TRUNK
#define TRUNK_ON		PWR_KEY_PORT |= TRUNK
//-------------- sygna³ SUPPLY -----------------
#define SUPPLY			(1<<PB0)

#define SUPPLY_ON		PWR_KEY_PORT |= SUPPLY
#define SUPPLY_OFF		PWR_KEY_PORT &= ~SUPPLY
//-------------- sygna³ LIMIT -----------------
#define LIMIT			(1<<PB2)

#define LIMIT_ON		PWR_KEY_PORT |= LIMIT
#define LIMIT_OFF		PWR_KEY_PORT &= ~LIMIT
//-------------- sygna³ ALARM -----------------
#define ALARM			(1<<PB4)

#define ALARM_ON		PWR_KEY_PORT |= ALARM
#define ALARM_OFF		PWR_KEY_PORT &= ~ALARM

char *GPS_READY;
char *Lattitude;
char *NS;
char *Longitude;
char *EW;
char *TOKEN;
char *STATE;
char *battery;
char engine;
char alarm;

enum { _timeout = -1 , _void = 0, _search = 1, _gprmc = 2, _post = 3, _reconnect = 4, _test = 5, _restart = 6};
enum { _sms_data_send_error=-3, _at_timeout=-2, _at_error=-1, _at_ok=0, _at_idle=1, _at_start=2 };
enum {_off = 0 , _on = 1};


typedef struct {
	uint8_t semaphore;
	uint8_t atcmd;
	int8_t status;
	uint16_t timeout;
	uint16_t tmo_cnt;
} TATPROCESS;

typedef struct {
		int8_t status;
		int8_t tmo_cnt;
		int8_t eng_st;
} GPSPROCESS;
//******* zmienne globalne ****************
extern TATPROCESS atprocess;
extern GPSPROCESS gpsprocess;





//******* nag³ówki funkcji ****************
uint8_t send_at( uint8_t at, const char * pgm_par, char * ram_par, uint16_t tmo );
int8_t wait_for_at_process_end( void );
int8_t wait_for_site_process_end( void );
uint8_t wait_for_site(uint16_t tmo );


#endif /* MK_GSM_MK_GSM_H_ */
