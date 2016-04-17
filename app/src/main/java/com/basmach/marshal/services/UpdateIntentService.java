package com.basmach.marshal.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CHECK_FOR_UPDATE = "com.basmach.marshal.utils.action.CHECK_FOR_UPDATE";
    public static final String ACTION_UPDATE_DATA = "com.basmach.marshal.utils.action.UPDATE_DATA";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.basmach.marshal.utils.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.basmach.marshal.utils.extra.PARAM2";

    public static final String RESULT_CHECK_FOR_UPDATE = "result_check_for_update";

    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startCheckForUpdate(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_CHECK_FOR_UPDATE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUpdateData(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE_DATA);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_FOR_UPDATE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCheckForUpdate(param1, param2);
            } else if (ACTION_UPDATE_DATA.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdateData(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckForUpdate(String param1, String param2) {
        // TODO: Handle action Foo
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent broadcastIntent = new Intent();
//                broadcastIntent.setAction(ACTION_CHECK_FOR_UPDATE);
//                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//                broadcastIntent.putExtra(RESULT_CHECK_FOR_UPDATE, false);
//                sendBroadcast(broadcastIntent);
//            }
//        }, 1000);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CHECK_FOR_UPDATE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESULT_CHECK_FOR_UPDATE, false);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateData(String param1, String param2) {
        // TODO: Handle action Baz

    }
}
