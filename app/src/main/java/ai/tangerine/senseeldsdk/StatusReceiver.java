package ai.tangerine.senseeldsdk;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import ai.tangerine.eldsdk.ELDConstants;
import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.eldsdk.ELDSdkIllegalStateException;

public class StatusReceiver extends BroadcastReceiver {

    private static final String TAG = "StatusReceiver";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // todo step-10-b
        String action = intent.getAction();
        if(action != null) {
            Log.i(TAG, "action: "+ action);
            switch (action) {
                case ELDConstants.BROADCAST_INIT_SDK:
                    try {
                        ELDSdk.init(context, "ELD-SAMPLE", R.drawable.ic_notification);
                    } catch (ELDSdkIllegalStateException e) {
                        e.printStackTrace();
                    }
                    break;

                case ELDConstants.BROADCAST_START_CONNECTION:
                    if (!ELDSdk.isConnected()) {
                        Log.i(TAG, "connecting...");
                        intent = new Intent(context, DataService.class);
                        intent.setAction(DataService.ACTION_CONNECT);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(context, intent);
                        } else {
                            context.startService(intent);
                        }
                    } else {
                        Log.i(TAG, "already connected");
                    }
                    break;
            }
        }
    }
}
