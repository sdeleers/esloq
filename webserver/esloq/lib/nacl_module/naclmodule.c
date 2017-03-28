#include <Python.h>
#include "nacl/build/slock2/include/amd64/crypto_secretbox.h"
#include "nacl/build/slock2/include/amd64/crypto_onetimeauth.h"

/* TODO use y* instead of y# http://docs.python.org/3.4/c-api/arg.html */

static PyObject* auth_encrypt(PyObject* self, PyObject* args) {
    const unsigned char *m; unsigned int mlen;
    const unsigned char *n; unsigned int nlen;
    const unsigned char *k; unsigned int klen;
    int i;

    /* Parse Python arguments to C arguments */
    if (!PyArg_ParseTuple(args, "y#y#y#", &m, &mlen, &n, &nlen, &k, &klen)) {
        return NULL;
    }

    /* Initialize ciphertext to zero. */
    unsigned int clen = mlen + crypto_onetimeauth_poly1305_BYTES;
    unsigned char c[crypto_secretbox_BOXZEROBYTES + clen];
    for(i = 0; i < crypto_secretbox_BOXZEROBYTES+clen; i++) {
        c[i] = 0;
    }

    /* Secretbox expects leading zeros before the message. Store this padded 
     * version of the message in m_secretbox. Also, initialize m_secretbox to 
     * zeros first.*/
    unsigned char m_secretbox[crypto_secretbox_ZEROBYTES + mlen];
        for(i = 0; i < crypto_secretbox_ZEROBYTES+mlen; i++) {
            m_secretbox[i] = 0;
        }
    memcpy(m_secretbox+crypto_secretbox_ZEROBYTES, m, mlen);

    /* Authenticated encryption */
    if(crypto_secretbox(c, m_secretbox, mlen+crypto_secretbox_ZEROBYTES, n, k) != 0) {
        return NULL;
    }

    /* Return ciphertext without leading zeros. */
    return Py_BuildValue("y#", c+crypto_secretbox_BOXZEROBYTES, clen);
}

static PyObject* auth_decrypt(PyObject* self, PyObject* args) {
    const unsigned char *c; unsigned int clen;
    const unsigned char *n; unsigned int nlen;
    const unsigned char *k; unsigned int klen;
    int i;
    
    /* Parse Python arguments to C arguments */
    if (!PyArg_ParseTuple(args, "y#y#y#", &c, &clen, &n, &nlen, &k, &klen)) {
        return NULL;
    }

    /* Initialize plaintext to zero. */
    unsigned int mlen = clen - crypto_onetimeauth_poly1305_BYTES;
    unsigned char m[crypto_secretbox_ZEROBYTES + mlen];
    for(i = 0; i < crypto_onetimeauth_poly1305_BYTES+mlen; i++) {
        m[i] = 0;
    }

    /* Secretbox expects leading zeros before the ciphertext. Store this padded 
     * version of the message in c_secretbox. Also, initialize c_secretbox to 
     * zeros first.*/
    unsigned char c_secretbox[crypto_secretbox_BOXZEROBYTES + clen];
    for(i = 0; i < crypto_secretbox_BOXZEROBYTES+clen; i++) {
        c_secretbox[i] = 0;
    }
    memcpy(c_secretbox+crypto_secretbox_BOXZEROBYTES, c, clen);

    /* Authenticated decryption */
    if(crypto_secretbox_open(m, c_secretbox, clen+crypto_secretbox_BOXZEROBYTES, n, k) != 0) {
        return NULL;
    }

    /* Return plaintext without leading zeros. */
    return Py_BuildValue("y#", m+crypto_secretbox_ZEROBYTES, mlen);
}

static PyMethodDef nacl_methods[] = {
    {"auth_encrypt", auth_encrypt, METH_VARARGS, "Authenticated encryption"},
    {"auth_decrypt", auth_decrypt, METH_VARARGS, "Authenticated decryption"},
    {NULL, NULL, 0, NULL}
};

/* For python 2.7 */
/*PyMODINIT_FUNC initnaclmodule(void) {
    (void) Py_InitModule("naclmodule", nacl_methods);
}*/

/* For python 3.x */
static struct PyModuleDef naclmodule = {
    PyModuleDef_HEAD_INIT,
    "nacl", /* name of module */
    NULL,   /* module documentation, may be NULL */
    -1,     /* size of per-interpreter state of the module, or -1 if the module keeps state in global variables. */
    nacl_methods
};

PyMODINIT_FUNC PyInit_nacl(void) {
        return PyModule_Create(&naclmodule);
}
