package com.esloq.esloqapp.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The URLS used to make server requests.
 */
public class Urls {

    public static final URL GET_USER_DATA;
    public static final URL ADD_USER;
    public static final URL INITIALIZE_LOCK;
    public static final URL RESET_LOCK;
    public static final URL REQUEST_SESSION_KEY;
    public static final URL ADD_LOG;
    public static final URL REMOVE_USER;
    public static final URL ADD_REGISTRATION_TOKEN;
    public static final URL LOG;

    private static final String protocol = "https";
    private static final String host = "api.esloq.com";
    private static final int port = 443;
//    private static final String protocol = "http";
//    private static final String host = "192.168.1.133";
//    private static final int port = 8000;

    static {
        URL getUserData = null;
        URL addUser = null;
        URL setLockAdmin = null;
        URL resetLock = null;
        URL requestSessionKey = null;
        URL addLog = null;
        URL removeUser = null;
        URL addRegistrationToken = null;
        URL log = null;
        try{
            getUserData = new URL(protocol, host, port, "getuserdata/");
            addUser = new URL(protocol, host, port, "adduser/");
            setLockAdmin  = new URL(protocol, host, port, "initializelock/");
            resetLock = new URL(protocol, host, port, "resetlock/");
            requestSessionKey = new URL(protocol, host, port, "requestsessionkey/");
            addLog = new URL(protocol, host, port, "addlog/");
            removeUser = new URL(protocol, host, port, "removeuser/");
            addRegistrationToken = new URL(protocol, host, port, "addregistrationtoken/");
            log =  new URL(protocol, host, port, "log/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        GET_USER_DATA = getUserData;
        ADD_USER = addUser;
        INITIALIZE_LOCK = setLockAdmin;
        RESET_LOCK = resetLock;
        REQUEST_SESSION_KEY = requestSessionKey;
        ADD_LOG = addLog;
        REMOVE_USER = removeUser;
        ADD_REGISTRATION_TOKEN = addRegistrationToken;
        LOG = log;
    }
}
