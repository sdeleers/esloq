
#include <android/log.h>
#include "com_esloq_esloqapp_Cryptography.h"
#include "avrnacl.h"

#define TAG "CryptoNaclJni"


/*
 * Class:     com_esloq_esloqapp_Cryptography
 * Method:    encrypt
 * Signature: ([B[B[B)[B
 */
 JNIEXPORT jbyteArray JNICALL Java_com_esloq_esloqapp_Cryptography_encrypt
   (JNIEnv *env, jclass cls, jbyteArray message, jbyteArray nonce, jbyteArray key) {
    unsigned int mlen = (*env)->GetArrayLength(env,message);
    unsigned int clen = mlen + crypto_onetimeauth_poly1305_BYTES;
    unsigned char m[crypto_secretbox_ZEROBYTES  + mlen];
    unsigned char c[crypto_secretbox_BOXZEROBYTES  + clen];
    const unsigned char n[crypto_secretbox_NONCEBYTES];
    const unsigned char k[crypto_secretbox_KEYBYTES];
    int i;

    if (mlen == 0 || (*env)->GetArrayLength(env,nonce) != crypto_secretbox_NONCEBYTES ||
        (*env)->GetArrayLength(env,key) != crypto_secretbox_KEYBYTES) {
        return (*env)->NewByteArray(env,0);
    }

    /* Create native message m with 32 leading zeros */
    for (i = 0; i < crypto_secretbox_ZEROBYTES; i++) m[i] = 0;
    (*env)->GetByteArrayRegion(env, message, 0, mlen, (jbyte*) m + crypto_secretbox_ZEROBYTES);

    /* Create native nonce n */
    (*env)->GetByteArrayRegion(env, nonce, 0, crypto_secretbox_NONCEBYTES, (jbyte*) n);

    /* Create native key k */
    (*env)->GetByteArrayRegion(env, key, 0, crypto_secretbox_KEYBYTES, (jbyte*) k);

    /* Encrypt the message using the provided nonce and key, return a zero when failed */
    if (crypto_secretbox(c,m,crypto_secretbox_ZEROBYTES+mlen,n,k) != 0) {
        return (*env)->NewByteArray(env,0);
    }

    /* Return the ciphertext */
    jbyteArray ciphertext = (*env)->NewByteArray(env, clen);
    (*env)->SetByteArrayRegion(env, ciphertext, 0, clen, (jbyte*) c + crypto_secretbox_BOXZEROBYTES);
    return ciphertext;
}

/*
 * Class:     com_esloq_esloqapp_Cryptography
 * Method:    decrypt
 * Signature: ([B[B[B)[B
 */
 JNIEXPORT jbyteArray JNICALL Java_com_esloq_esloqapp_Cryptography_decrypt
   (JNIEnv *env, jclass cls, jbyteArray ciphertext, jbyteArray nonce, jbyteArray key) {
    unsigned int clen = (*env)->GetArrayLength(env,ciphertext);
    unsigned int mlen = clen - crypto_onetimeauth_poly1305_BYTES;
    unsigned char m[crypto_secretbox_ZEROBYTES  + mlen];
    unsigned char c[crypto_secretbox_BOXZEROBYTES  + clen];
    const unsigned char n[crypto_secretbox_NONCEBYTES];
    const unsigned char k[crypto_secretbox_KEYBYTES];
    int i;

    if (clen <= crypto_onetimeauth_poly1305_BYTES ||
        (*env)->GetArrayLength(env,nonce) != crypto_secretbox_NONCEBYTES ||
        (*env)->GetArrayLength(env,key) != crypto_secretbox_KEYBYTES) {
        return (*env)->NewByteArray(env,0);
    }

    /* Create native ciphertext m with 16 leading zeros */
    for (i = 0; i < crypto_secretbox_BOXZEROBYTES; i++) c[i] = 0;
    (*env)->GetByteArrayRegion(env, ciphertext, 0, clen, (jbyte*) c + crypto_secretbox_BOXZEROBYTES);

    /* Create native nonce n */
    (*env)->GetByteArrayRegion(env, nonce, 0, crypto_secretbox_NONCEBYTES, (jbyte*) n);

    /* Create native key k */
    (*env)->GetByteArrayRegion(env, key, 0, crypto_secretbox_KEYBYTES, (jbyte*) k);

    /* Encrypt the message using the provided nonce and key, return a zero when failed */
    if (crypto_secretbox_open(m,c,crypto_secretbox_BOXZEROBYTES+clen,n,k) != 0) {
        return (*env)->NewByteArray(env,0);
    }

    /* Return the plaintext */
    jbyteArray plaintext = (*env)->NewByteArray(env, mlen);
    (*env)->SetByteArrayRegion(env, plaintext, 0, mlen, (jbyte*) m + crypto_secretbox_ZEROBYTES);
    return plaintext;
}
