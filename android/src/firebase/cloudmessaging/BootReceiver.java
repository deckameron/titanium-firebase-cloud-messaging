package firebase.cloudmessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "FCM.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted - refreshing FCM token");

            // Força refresh do token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d(TAG, "Token refreshed after boot");
                }
            });
        }
    }
}