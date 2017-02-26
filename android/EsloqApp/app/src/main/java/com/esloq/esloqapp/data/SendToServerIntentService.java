package com.esloq.esloqapp.data;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SendToServerIntentService extends IntentService {

    public static final String RESPONSE = "com.esloq.esloqapp.response";

    /**
     * Integers to report if the service executed successfully or not.
     */
    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;

    private static final String TAG = SendToServerIntentService.class.getSimpleName();

    /**
     * String representation of the actions that can be requested in this IntentService.
     */
    private static final String ACTION_POST = "com.esloq.esloqapp.action.POST";
    private static final String ACTION_GET = "com.esloq.esloqapp.action.GET";

    /**
     * String representation of the extra data that can be given to this IntentService.
     */
    private static final String EXTRA_RECEIVER = "com.esloq.esloqapp.extra.RECEIVER";
    private static final String EXTRA_URL = "com.esloq.esloqapp.extra.URL";
    private static final String EXTRA_DATA = "com.esloq.esloqapp.extra.DATA";

    public SendToServerIntentService() {
        super(TAG);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPostToServer(Context context, ResultReceiver resultReceiver, URL url, JSONObject jsonObject) {
        Intent intent = new Intent(context, SendToServerIntentService.class);
        intent.setAction(ACTION_POST);
        intent.putExtra(EXTRA_RECEIVER, resultReceiver);
        intent.putExtra(EXTRA_URL, url.toString());
        intent.putExtra(EXTRA_DATA, jsonObject.toString());
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetFromServer(Context context, ResultReceiver resultReceiver, URL url) {
        Intent intent = new Intent(context, SendToServerIntentService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_RECEIVER, resultReceiver);
        intent.putExtra(EXTRA_URL, url.toString());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            final String action = intent.getAction();
            if (ACTION_POST.equals(action)) {
                try {
                    final URL url = new URL(intent.getStringExtra(EXTRA_URL));
                    final JSONObject data = new JSONObject(intent.getStringExtra(EXTRA_DATA));
                    handleActionPost(receiver, url, data);
                } catch (MalformedURLException | JSONException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_GET.equals(action)) {
                try {
                    final URL url = new URL(intent.getStringExtra(EXTRA_URL));
                    handleActionGet(receiver, url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPost(ResultReceiver resultReceiver, URL url, JSONObject jsonObject) {
        try {
            JSONObject httpResponse = HttpsCommunication.postJson(getApplicationContext(), url, jsonObject);
            Bundle bundle = new Bundle();
            if (httpResponse != null) {
                bundle.putString(RESPONSE, httpResponse.toString());
                resultReceiver.send(RESULT_OK, bundle);
            } else {
                resultReceiver.send(RESULT_ERROR, bundle);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGet(ResultReceiver resultReceiver, URL url) {
            try {
                JSONObject httpResponse = HttpsCommunication.getJson(getApplicationContext(), url);
                Bundle bundle = new Bundle();
                if (httpResponse != null) {
                    bundle.putString(RESPONSE, httpResponse.toString());
                    resultReceiver.send(RESULT_OK, bundle);
                } else {
                    resultReceiver.send(RESULT_ERROR, bundle);
                }
            }
            catch (JSONException | IOException e) {
                e.printStackTrace();
            }
    }
}
