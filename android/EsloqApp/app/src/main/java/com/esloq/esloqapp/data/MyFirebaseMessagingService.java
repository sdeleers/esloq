package com.esloq.esloqapp.data;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.Map;

/**
 * Listens for and handles GCMs received from the server.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();


    /**
     * Called when message is received.
     *
     * @param message The message received from the backend server.
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map<String, String> data = message.getData();
        Log.i(TAG, "Message from: " + from + " data: " + data.toString());

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        if (!data.isEmpty()) {
            handleData(data);
        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
//        sendNotification(message);
    }

//    /**
//     * Create and show a simple notification containing the received GCM message.
//     *
//     * @param message GCM message received.
//     */
//    private void sendNotification(String message) {
//        Intent intent = new Intent(this, HomeActivity.class);
////        Intent intent = new Intent(this, LockListActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                .setContentTitle("GCM Message")
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }

    /**
     * Handles the data received from the GCM. Usually this means adding or updating data to the
     * repository.
     *
     * @param data The data that needs to be handled.
     */
    private void handleData(Map<String, String> data) {
        PreferencesServiceApi preferences = new PreferencesServiceApiImpl(getApplicationContext());
        String myId = String.valueOf(preferences.getUserId());
        if (myId.equals(data.get("userId")) || Arrays.asList(data.get("userIds").split
                ("\\s*,\\s*")).contains(myId)) {
            LocalDataServiceApi localDataServiceApi = new LocalDataServiceApiImpl(getApplicationContext());
            String command = data.get("command");
            // TODO add users to lock in addlock, problem only key/values can be received. maybe asynctask to get users and logs?
            String lockMac;
            switch (command != null ? command : "") {
                case "updateUser":
                    localDataServiceApi.updateUser(Integer.valueOf(data.get("userId")), String
                            .valueOf(data.get("firstName")), true);
                    break;
                case "addUser":
                    lockMac = data.get("lockMac").toUpperCase();
                    // If I'm the user that got added, add lock.
                    if (myId.equals(data.get("addUserId"))) {
                        localDataServiceApi.addLock(lockMac, data.get("lockName"), Boolean
                                .valueOf(data.get("lockClockwise")));
                    }
                    localDataServiceApi.addUserToLock(Integer.valueOf(data.get("addUserId")), data
                            .get("addUserName"), Boolean.valueOf(data.get("addUserValidated")), lockMac,
                            Boolean.valueOf(data.get("isAdmin")));
                    break;
                case "removeUser":
                    lockMac = data.get("lockMac").toUpperCase();
                    if (myId.equals(data.get("removeUserId"))) {
                        localDataServiceApi.addLock(lockMac, data.get("lockName"), Boolean
                                .valueOf(data.get("lockClockwise")));
                    }
                    localDataServiceApi.removeUserFromLock(Integer.valueOf(data.get("removeUserId")),
                            lockMac);
                    break;
                case "addLog":
                    lockMac = data.get("lockMac").toUpperCase();
                    localDataServiceApi.addLogToLock(Integer.valueOf(data.get("logUserId")), lockMac,
                            Boolean.valueOf(data.get("lockState")),
                            Long.valueOf(data.get("timestamp")));
                    break;
                default:
                    Log.w(TAG, "Invalid FCM command received.");
            }
        }
    }
}
