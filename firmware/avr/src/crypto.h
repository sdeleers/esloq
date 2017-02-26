/** 
 * @file crypto.h
 *
 * @copydoc crypto.c
 */

#ifndef _CRYPTO_H_
#define _CRYPTO_H_

#include <avr/eeprom.h>
#include <stdint.h>
#include "avrnacl.h"

#define AUTH_LEN crypto_onetimeauth_BYTES
#define KEY_LEN crypto_secretbox_KEYBYTES
#define NONCE_LEN crypto_secretbox_NONCEBYTES

extern uint8_t EEMEM master_nonce_eeprom[NONCE_LEN];
extern uint8_t EEMEM master_key_eeprom[KEY_LEN];

extern uint8_t session_key[KEY_LEN];
extern uint8_t session_nonce[NONCE_LEN];

int8_t crypto_auth_encrypt(uint8_t *c, uint8_t *m, uint8_t mlen, uint8_t *n, uint8_t *k);
int8_t crypto_auth_decrypt(uint8_t *m, uint8_t *c, uint8_t clen, uint8_t *n, uint8_t *k);

#endif
