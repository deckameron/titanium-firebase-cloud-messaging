package firebase.cloudmessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.appcelerator.kroll.KrollDict;
import org.json.JSONObject;

import java.util.HashMap;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "FCM.ActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionId = intent.getStringExtra("action_id");
        String fcmData = intent.getStringExtra("fcm_data");

        Log.d(TAG, "Notification action received: " + actionId);

        CloudMessagingModule module = CloudMessagingModule.getInstance();

        if (module != null) {
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("actionId", actionId);

            // Tentar parsear os dados FCM
            if (fcmData != null && !fcmData.isEmpty()) {
                try {
                    JSONObject jsonData = new JSONObject(fcmData);
                    eventData.put("data", new KrollDict(jsonData));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing FCM data", e);
                }
            }

            // Disparar evento para o JavaScript
            module.fireEvent("notificationAction", new KrollDict(eventData));
        } else {
            Log.w(TAG, "CloudMessagingModule instance is null");
        }
    }
}