/*
 * common.h
 *
 *  Created on: 6 lip 2016
 *      Author: admin
 */

#ifndef COMMON_H_
#define COMMON_H_
int8_t res;

int bat;

enum { _tmr_at_process, _tmr1, _tmr2 };

/* makro zamieniaj¹ce podany argument na ³añcuch znaków */
#define TOSTRING( x ) STRINGIFY( x )
#define STRINGIFY( x ) #x
void check(int z);


extern char buf[];	// g³ówny bufor RAM dla zdarzenia UART EVENT

void adc_init(void);

uint16_t adc_read();

void MAIN_EVENTS( int8_t tmr_nr );
void mDelay( uint16_t ms );
//char battery(int level);
#endif /* COMMON_H_ */
