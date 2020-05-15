package ai.tangerine.senseeldsdk;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import ai.tangerine.eldsdk.ELDConstants;
import ai.tangerine.eldsdk.ELDListener;
import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.eldsdk.ELDSdkIllegalArgumentException;
import ai.tangerine.eldsdk.ELDSdkIllegalStateException;
import ai.tangerine.senseeldsdk.utils.NotificationUtils;

public class DataService extends Service {

    private static final String TAG = "DataService";
    public static final String ACTION_CONNECT = "action_connect";
    public static final String ACTION_DISCONNECT = "action_disconnect";

    private Intent mIntentService;
    private PendingIntent mPendingIntent;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to scan for devices
     */
    public static final int MSG_SCAN_FOR_DEVICE = 3;

    /**
     * Command to scan for devices
     */
    public static final int MSG_CLEAR_DEVICE = 4;

    /**
     * Command from service to send error data to binding activity
     */
    public static final int MSG_ERROR = 5;

    /**
     * Command from service to send state data to binding activity
     */
    public static final int MSG_STATE = 6;

    /**
     * Command from service to send ELD data to binding activity
     */
    public static final int MSG_DATA = 7;

    /**
     * Command from service to send ELD Device name to binding activity
     */
    public static final int MSG_DEVICE = 8;

    public DataService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIntentService = new Intent(this, DataService.class);
        mPendingIntent = PendingIntent.getBroadcast(this, 1,
            mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case ACTION_CONNECT:
                        connect();
                        return START_STICKY;

                    case ACTION_DISCONNECT:
                        disconnect();
                        return Service.START_NOT_STICKY;
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void connect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = NotificationUtils
                .getConnectionNotification(getApplicationContext());
            startForeground(NotificationUtils.NOTIFICATION_ID, notification);
        }
        requestConnect();
    }

    @SuppressLint("MissingPermission")
    private void scanForDevice() {
        // todo step-4-b
        // scan for nearby sense devices. Requires location permission to execute this method.
        // To connect to bluetooth and discover the nearby sense devices location permission is required.
        // Also, It requires Activity Recognition permission to auto connect to the ble android 10 onwards
        try {
            ELDSdk.scanForDevices(eldListener);
        } catch (ELDSdkIllegalStateException | ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    private void requestConnect() {
        // todo-6-b
        try {
            ELDSdk.connect(eldListener);
        } catch (ELDSdkIllegalStateException e) {
            e.printStackTrace();
        } catch (ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        // todo-7-b
        Log.i(TAG, "action disconnect");
        boolean isForegroundNotificationVisible = NotificationUtils
            .isForegroundNotificationVisible(getApplicationContext(),
                NotificationUtils.NOTIFICATION_ID);

        requestDisconnect();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isForegroundNotificationVisible) {
                stopForeground(true);
            }
        }
    }

    private void onDisconnect(){
        Log.i(TAG, "onDisconnect");
        boolean isForegroundNotificationVisible = NotificationUtils
            .isForegroundNotificationVisible(getApplicationContext(),
                NotificationUtils.NOTIFICATION_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isForegroundNotificationVisible) {
                stopForeground(true);
            }
        }
    }

    private void requestDisconnect() {
        // todo-7-c
        try {
            ELDSdk.disconnect(eldListener);
        } catch (ELDSdkIllegalStateException | ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void clearDevice() {
        // todo Step-8-a
        try {
            ELDSdk.clearDevice(eldListener);
        } catch (ELDSdkIllegalArgumentException | ELDSdkIllegalStateException e) {
            e.printStackTrace();
        }
    }

    ELDListener eldListener = new ELDListener() {
        @Override
        public void onStateChanged(int state) {
            String message = ELDConstants.getMessage(state);
            Log.i(TAG, "onStateChanged: " + state + ":" + message);
            sendErrorOrStateToBinder(MSG_STATE, state);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            switch (state) {
                case ELDConstants.STATE_CONNECTED:
                case ELDConstants.STATE_CONNECTING:
                case ELDConstants.DEVICE_CLEAR_SUCCESS:
                    break;
                case ELDConstants.STATE_DEVICE_NOT_FOUND:
                case ELDConstants.STATE_DISCONNECTED:
                    onDisconnect();
                    break;
            }
        }

        @Override
        public void onAccessError(int error) {

            String message = ELDConstants.getMessage(error);
            Log.i(TAG, "onAccessError: " + error + ":" + message);
            sendErrorOrStateToBinder(MSG_ERROR, error);
            switch (error) {
                case ELDConstants.ERROR_PERMISSION_DENIED_TO_CONNECT:
                case ELDConstants.ERROR_BT_NOT_ENABLED:
                case ELDConstants.ERROR_LOCATION_NOT_ENABLED:
                case ELDConstants.ERROR_LOCATION_PERMISSION_NOT_GRATED:
                case ELDConstants.ERROR_ACTIVITY_RECOGNITION_PERMISSION_NOT_GRATED:
                case ELDConstants.ERROR_LOCATION_PERMISSION_IN_BACK_NOT_GRATED:
                    onDisconnect();
                    break;
            }
        }

        @Override
        public void onDeviceFound(String bluetoothDeviceName) {
            Log.i(TAG, "onDeviceFound: " + bluetoothDeviceName);
            sendDataOrDeviceToBinder(MSG_DEVICE, bluetoothDeviceName);
        }

        @Override
        public void onDataReceived(String data) {
            Log.i(TAG, "onDataReceived: " + data);
            // todo-6-c you can save the ELD data here
            Toast.makeText(getApplicationContext(), "onDataReceived", Toast.LENGTH_SHORT).show();
            sendDataOrDeviceToBinder(MSG_DATA, data);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SCAN_FOR_DEVICE:
                    scanForDevice();
                    break;
                case MSG_CLEAR_DEVICE:
                    clearDevice();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendDataOrDeviceToBinder(int type, String value) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                    type, 0, 0, value));
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendErrorOrStateToBinder(int type, int value) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                    type, value, 0));
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }


}
