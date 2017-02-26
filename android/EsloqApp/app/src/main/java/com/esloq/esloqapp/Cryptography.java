package com.esloq.esloqapp;

import com.esloq.esloqapp.util.Tools;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Class containing the cryptographic functions needed to execute the protocol.
 */
public class Cryptography {

    /**
     * Name of the service class used for logging.
     */
    private static final String TAG = Cryptography.class.getSimpleName();

    /**
     * Length of the cryptographic nonce in bytes.
     */
    public static final int NONCE_LENGTH = 24;

    /**
     * Length of the Authenticator. Ciphertext adds this length to the plaintext in order to
     * perform authentication.
     */
    public static final int MAC_LENGTH = 16;

    private static final int TICKET_LENGTH = 72;
    private static final int KEY_LENGTH = 32;

    /**
     * The ticket received from server needed to authenticate user to the device.
     */
    private static byte[] lockTicket;

    /**
     * The key for this session, received from server.
     */
    private static byte[] sessionKey;

    /**
     * Last used session nonce.
     */
    private static byte[] sessionNonce = new byte[NONCE_LENGTH];

    /**
     * Get the lock ticket.
     *
     * @return The lock ticket.
     */
    public static byte[] getLockTicket() {
        return lockTicket;
    }

    /**
     * Set the lock ticket.
     *
     * @param ticket New lock ticket.
     */
    public static void setLockTicket(byte[] ticket) {
        if (ticket.length != TICKET_LENGTH) {
            throw new IllegalArgumentException("Ticket length expected: " + TICKET_LENGTH + ", " +
                    "actual: " + ticket.length);
        }
        lockTicket = ticket;
    }

    /**
     * Return the next nonce that will be used for encrypt/decrypt.
     *
     * @return Next session nonce that will be used to encrypt data.
     */
    public static byte[] getNextSessionNonce() {
        byte[] nextSessionNonce = sessionNonce.clone();
        Tools.incrementByteArray(nextSessionNonce);
        return nextSessionNonce;
    }

    /**
     * Set the new session nonce if it is a valid nonce i.e. larger than the current nonce.
     *
     * @param nonce New session nonce.
     * @throws IllegalArgumentException If the nonce is not greater than the last used nonce with
     * the same key.
     */
    public static void setSessionNonce(byte[] nonce) throws IllegalArgumentException {
        if(isValidSessionNonce(nonce)) {
            sessionNonce = nonce;
        }
        else {
            throw new IllegalArgumentException("Session nonce is not valid.");
        }
    }

    /**
     * Set the new session key and full <code>sessionNonce</code> with zeros.
     *
     * @param key New session key.
     */
    public static void setSessionKey(byte[] key){
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Key length expected: " + KEY_LENGTH + ", " +
                    "actual: " + key.length);
        }
        sessionKey = key;
        Arrays.fill(sessionNonce, (byte) 0);
    }

    /**
     * Returns mac | ciphertext.
     *
     * @param plaintext Bytes to be encrypted.
     * @return Authenticator followed by ciphertext if successful, null otherwise.
     */
    public static byte[] authEncrypt(byte[] plaintext) {
        Tools.incrementByteArray(sessionNonce);
        return encrypt(plaintext, sessionNonce, sessionKey);
    }

    /**
     * Returns plaintext.
     *
     * @param ciphertext Bytes to be decrypted.
     * @return The plaintext if successful, null otherwise.
     */
    public static byte[] authDecrypt(byte[] ciphertext) {
        return decrypt(ciphertext, sessionNonce, sessionKey); // convert errors (return null)
    }

    /**
     * Return true if nonce is greater than latest session nonce, false otherwise.
     *
     * @param nonce Bytes to be check if they are valid.
     * @return True if nonce is valid, false otherwise.
     */
    private static boolean isValidSessionNonce(byte[] nonce) {
        if (nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("Nonce length expected: " + NONCE_LENGTH + ", " +
                    "actual: " + nonce.length);
        }
        return new BigInteger(sessionNonce).compareTo(new BigInteger(nonce)) == -1;
    }

    /**
     * JNI wrapper for the encrypt method.
     *
     * @param message Bytes to be encrypted.
     * @param nonce Nonce to be used for the encryption.
     * @param key Key to be used for the encryption.
     * @return Message encrypted with key and nonce.
     */
    private native static byte[] encrypt(byte[] message, byte[] nonce, byte[] key);

    /**
     * JNI wrapper for the decrypt method.
     *
     * @param ciphertext Bytes to be decrypted.
     * @param nonce Nonce to be used for the decryption.
     * @param key Key to be used for the decryption.
     * @return Ciphertext decrypted with key and nonce.
     */
    private native static byte[] decrypt(byte[] ciphertext, byte[] nonce, byte[] key);

    /**
     * Load the crypto NaCl JNI library.
     */
    static {
        System.loadLibrary("crypto_nacl_jni");
    }
}
