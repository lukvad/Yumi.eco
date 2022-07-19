/*
 * mk_atcmdlist.c
 *
 *  Created on: 28 cze 2016
 *      Author: admin
 */
#include <avr/io.h>
#include <avr/pgmspace.h>

#include "mk_gsm.h"


//--- podstawowe komendy modu³u GSM
const char at_[] PROGMEM = "AT";		// podstawowa komenda testowa modemu
const char ate[] PROGMEM = "E0";// wy³¹czenie echa
const char gps[] PROGMEM = "+CGNSPWR=1";
const char creg[] PROGMEM = "+CIFSR";	// sprawdzanie stanu po³¹czenia z sieci¹ i stanu logowania do sieci
const char cmgs[] PROGMEM = "+CMGS=\"530184485\"";	// wys³anie SMSa
const char cmgf[] PROGMEM = "+CMGF=1";	// ustawienie SMSa w tryb PDU lub TXT
const char cgatt[] PROGMEM = "+CGATT=1";
const char qcgatt[] PROGMEM = "+CIPSTATUS";
const char ciicr[] PROGMEM = "+CIICR";
//const char cstt[] PROGMEM = "+CGDCONT=1,\"IP\",\"internet\"";
const char cstt[] PROGMEM = "+CSTT=\"plus\",\"plusgsm\",\"plusgsm\"";
const char qcgact[] PROGMEM = "Connection: keep-alive";
const char cipstart[] PROGMEM = "+CIPSTART=\"TCP\",\"lukvad.usermd.net\",80";
const char cipsend[] PROGMEM = "+CIPSEND";
const char get_token[] PROGMEM = "GET /945229/0";
const char get_slash[] PROGMEM =	"/";
const char http[] PROGMEM = " HTTP/1.1";
const char host[] PROGMEM = "Host: lukvad.usermd.net";
const char gpsrd[] PROGMEM = "+CGNSINF";
const char enter[] PROGMEM = "\r\n";
const char cipclose[] PROGMEM = "+CIPCLOSE";


const char * const atlist[] PROGMEM = {
		at_, ate, gps, creg, cmgs, cmgf, cgatt, qcgatt, ciicr, cstt, qcgact, cipstart, cipsend, get_token, get_slash, http, host, gpsrd, enter, cipclose
};


