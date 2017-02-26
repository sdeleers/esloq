/** 
 * @file crypto.c
 *
 * Wrapper around avrnacl's symmetric authenticated encrypt/decrypt functions.
 *
 */

#include <string.h>
#include "crypto.h"

/* Necessary because ISO C90 doesn't allow variable length arrays */
#define CRYPTO_MAX_MLEN 40
#define CRYPTO_MAX_CLEN (CRYPTO_MAX_MLEN+AUTH_LEN)

uint8_t EEMEM master_nonce_eeprom[NONCE_LEN] = {0};
uint8_t EEMEM master_key_eeprom[KEY_LEN] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};

uint8_t session_nonce[NONCE_LEN] = {0};
uint8_t session_key[KEY_LEN] = {0};

/**
 * Authenticated encrypt.
 *
 * Preforms authenticated encryption on plaintext using xsalsa20poly1305.
 *
 * @param[out] c    ciphertext, encrypted m + AUTH_LEN authenticator
 * @param[in]  m    plaintext, message to encrypt
 * @param[in]  mlen length of m
 * @param[in]  n    NONCE_LEN nonce used for encryption
 * @param[in]  k    KEY_LEN key used for encryption
 * @return 0 when successful, -1 otherwise
 */
int8_t crypto_auth_encrypt(uint8_t *c, uint8_t *m, uint8_t mlen, uint8_t *n, uint8_t *k) {
    uint8_t m_secretbox[CRYPTO_MAX_MLEN + crypto_secretbox_ZEROBYTES] = {0};
    uint8_t c_secretbox[CRYPTO_MAX_CLEN + crypto_secretbox_BOXZEROBYTES] = {0};
    int8_t return_value = 0;

    /* Copy m to m_secretbox with leading zeros. */
    memcpy(m_secretbox+crypto_secretbox_ZEROBYTES, m, mlen);

    /* Encrypt */
    return_value = crypto_secretbox(c_secretbox, m_secretbox, mlen+crypto_secretbox_ZEROBYTES, n, k);

    /* Copy c_secretbox to c without leading zeros */
    memcpy(c, c_secretbox+crypto_secretbox_BOXZEROBYTES, mlen+AUTH_LEN);

    return return_value;
}

/**
 * Authenticated decrypt.
 *
 * Preforms authenticated decryption on ciphertext using xsalsa20poly1305.
 *
 * @param[out] m    plaintext, decrypted c
 * @param[in]  c    ciphertext, message to decrypt
 * @param[in]  clen length of c (mlen + AUTH_LEN)
 * @param[in]  n    NONCE_LEN nonce used for decryption
 * @param[in]  k    KEY_LEN key used for decryption
 * @return 0 when successful, -1 otherwise
 */
int8_t crypto_auth_decrypt(uint8_t *m, uint8_t *c, uint8_t clen, uint8_t *n, uint8_t *k) {
    uint8_t m_secretbox[CRYPTO_MAX_MLEN + crypto_secretbox_ZEROBYTES] = {0};
    uint8_t c_secretbox[CRYPTO_MAX_CLEN + crypto_secretbox_BOXZEROBYTES] = {0};
    int8_t return_value = 0;

    /* Copy c to c_secretbox with leading zeros. */
    memcpy(c_secretbox+crypto_secretbox_BOXZEROBYTES, c, clen);

    /* Decrypt */
    return_value = crypto_secretbox_open(m_secretbox, c_secretbox, clen+crypto_secretbox_BOXZEROBYTES, n, k);

    /* Copy m_secretbox to m without leading zeros. */
    memcpy(m, m_secretbox+crypto_secretbox_ZEROBYTES, clen-AUTH_LEN);

    return return_value;
}
