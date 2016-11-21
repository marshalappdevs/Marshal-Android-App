package com.basmapp.marshal.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FcmIntentService extends FirebaseMessagingService {

    SharedPreferences mSharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Map data = message.getData();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        NotificationUtils mNotificationUtils = new NotificationUtils(this);

        Intent notifyIntent =
                new Intent(this, MainActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        String type = (String) data.get("type");

        if (type != null) {
            switch (type) {
                case "commands":
                    String[] commands = ((String) data.get("commands")).replace("\"", "").replace("[", "")
                            .replace("]", "").split(",");
                    if (commands != null) {
                        executeCommands(commands);
                    }
                    break;
                case "notification":
                    // Get Title
                    String title = (String) data.get("title");
                    if (title == null) title = getString(R.string.app_name);
                    // Get Content
                    String content = (String) data.get("content");
                    // Get PhotoUrl
                    String imageUrl = (String) data.get("imageUrl");

                    if (content != null) {
                        if (imageUrl != null) {
                            // Show Picture Notification
                            new NotificationUtils.GeneratePictureStyleNotification(this, content,
                                    imageUrl, notifyPendingIntent);
                        } else {
                            // Show Basic Notification
                            mNotificationUtils.notify(title, content, notifyPendingIntent);
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private void executeCommands(String[] commands) {
        for (String command : commands) {
            executeCommand(command);
        }
    }

    private void executeCommand(String command) {
        switch (command) {
            case "set-registration-state?false":
                boolean state = Boolean.valueOf(command.split("\\?")[1]);
                FcmRegistrationService.setDeviceRegistrationState(this, state);
                break;
            case "start-data-update":
                UpdateIntentService.startUpdateData(this);
                break;
            case "set-must-update?true":
                boolean value = Boolean.valueOf(command.split("\\?")[1]);
                mSharedPreferences.edit().putBoolean(Constants.PREF_MUST_UPDATE, value).apply();
                break;
            default:
                break;
        }
    }
}
