/*
 * mk_atcmdlist.h
 *
 *  Created on: 28 cze 2016
 *      Author: admin
 */



#ifndef MK_GSM_MK_ATCMDLIST_H_
#define MK_GSM_MK_ATCMDLIST_H_

#include "mk_gsm.h"

enum { _at, _ate, _gps, _creg, _cmgs, _cmgf, _cgatt,_qcgatt, _ciicr,_cstt, _qcgact, _cipstart, _cipsend, _get_token, _get_slash, _http, _host, _gpsrd, _enter, _cipclose };

extern const char * const atlist[];


#endif /* MK_GSM_MK_ATCMDLIST_H_ */
