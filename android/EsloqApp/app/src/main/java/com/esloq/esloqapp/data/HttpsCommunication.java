package com.esloq.esloqapp.data;

import android.content.Context;
import android.util.Log;

import com.esloq.esloqapp.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

/**
 * Static class containing methods for interacting with an HTTP web server.
 */
class HttpsCommunication {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = HttpsCommunication.class.getSimpleName();

    /**
     * Makes an HTTP GET request to a web server at the given URL and returns the server's response.
     * This method assumes that the web server returns a JSON response. If not, null is returned.
     * Makes use of <code>readJSON</code> to read the web server's JSON response.
     * @param url   Web address for the GET request.
     * @return      JSON data returned by the web server.
     * @throws      java.io.IOException when URL invalid or InputStream can't be created or read.
     * @throws      org.json.JSONException when response is not a valid JSON.
     */
    public static JSONObject getJson(Context context, URL url) throws IOException, JSONException {
        JSONObject jsonResponse = null;
        HttpURLConnection urlConnection;
        if (BuildConfig.DEBUG && url.getProtocol().equals("http")) {
            urlConnection = (HttpURLConnection) url.openConnection();
        } else {
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            urlConnection = (HttpsURLConnection) url.openConnection();
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(getSslContext(context)
                    .getSocketFactory());
        }
        try {
            jsonResponse = readJson(urlConnection);
        }
        finally {
            urlConnection.disconnect();
        }
        return jsonResponse;
    }

    /**
     * Makes an HTTP POST request to a web server at the given URL and returns the server's response.
     * The body of the POST request is JSON-encoded. Makes use of <code>writeJSON</code> and
     * <code>readJSON</code> to write and read the JSON-encoded data.
     * @param url   Web address for the GET request.
     * @param param The JSON-encoded request data.
     * @return      Response of the web server.
     * @throws IOException If there is a problem with the url connection.
     * @throws JSONException If there is an error in the JSON formatting of the server's response.
     */
    public static JSONObject postJson(Context context, URL url, JSONObject param) throws IOException, JSONException {
        JSONObject jsonResponse = null;
        HttpURLConnection urlConnection;
        if (BuildConfig.DEBUG && url.getProtocol().equals("http")) {
            urlConnection = (HttpURLConnection) url.openConnection();
        } else {
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            urlConnection = (HttpsURLConnection) url.openConnection();
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(getSslContext(context)
                    .getSocketFactory());
        }
        urlConnection.setDoOutput(true);
        try {
            writeJson(urlConnection, param);
            jsonResponse = readJson(urlConnection);
        }
        finally {
            urlConnection.disconnect();
        }
        return jsonResponse;
    }

    /**
     * Return the <code>SSLContext</code> used for the TLS connection settings. The SSL
     * communication uses certificate pinning.
     *
     * @param context The context of this application
     * @return SSLContext used for the TLS connection settings.
     * @throws SSLException When there is an error with the TLS settings.
     */
    private static SSLContext getSslContext(Context context) throws SSLException {
        try {
            // Load X.509 certificate from file api_esloq.crt in main/assets
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            try (InputStream caInput = new BufferedInputStream(context.getAssets().open("api_esloq.crt"))) {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
//            String[] suites = sslContext.createSSLEngine().getEnabledCipherSuites();
//            for (String suite: suites) {
//                Log.e("cipher", suite);
//            }

            return sslContext;

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException
                | KeyManagementException | IOException e) {
            e.printStackTrace();
            throw new SSLException("Error getting SSLContext.");
        }
    }

    /**
     * Reads JSON-encoded data from an HttpUrlConnection.
     *
     * @param urlConnection The HttpURLConnection to read from.
     * @return JSON response from server.
     * @throws IOException If there is a problem with the url connection.
     * @throws JSONException If there is an error in the JSON formatting of the server's response.
     */
    private static JSONObject readJson(HttpURLConnection urlConnection) throws IOException, JSONException {
        int status = urlConnection.getResponseCode();
        if (BuildConfig.DEBUG) Log.d(TAG, "HTTP status code: " + String.valueOf(status));

        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); //Trick to be able to use readline, slow, converts bytes to chars
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new JSONObject(response.toString());
        }
        else {
            return null;
        }
    }

    /**
     * Writes JSON-encoded data to an HttpURLConnection.
     *
     * @param urlConnection The HttpURLConnection to write to.
     * @param param         JSON-encoded data to be sent to server.
     * @throws IOException If there is a problem with the url connection.
     */
    private static void writeJson(HttpURLConnection urlConnection, JSONObject param) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream())); //Trick to be able to use readline, slow, converts bytes to chars
        out.write(param.toString());
        out.close();
    }

}
