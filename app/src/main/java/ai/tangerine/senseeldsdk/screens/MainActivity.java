package ai.tangerine.senseeldsdk.screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import ai.tangerine.eldsdk.ELDConstants;
import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.senseeldsdk.DataService;
import ai.tangerine.senseeldsdk.R;


public class MainActivity extends BaseEldActivity {

    private AppCompatTextView txtData;
    private AppCompatButton btnConnect;
    private AppCompatButton btnDisconnect;
    private AppCompatButton btnClearDevice;
    private static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtData = findViewById(R.id.data_text);
        btnConnect = findViewById(R.id.btn_connect);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnClearDevice = findViewById(R.id.btn_clear_device);

        btnConnect.setOnClickListener(view -> connect());

        btnClearDevice.setOnClickListener(view -> clearDevice());

        btnDisconnect.setOnClickListener(view -> disconnect());

        checkConnected();

    }

    private void checkConnected() {
        // todo check for already connected to avoid re-connect
        if(ELDSdk.isConnected()) {
            showConnectBtn(false);
            showDisconnectBtn(true);
        } else {
            showConnectBtn(true);
            showDisconnectBtn(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void connect() {
        // todo Step-6-a

        if(!ELDSdk.isConnected()) {
            showProgressBar(true);
            Intent intent = new Intent(getApplicationContext(), DataService.class);
            intent.setAction(DataService.ACTION_CONNECT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(getApplicationContext(), intent);
            } else {
                startService(intent);
            }
        }
    }
    

    private void clearDevice() {
        sendCommandToService(DataService.MSG_CLEAR_DEVICE);
    }

    public void disconnect() {
        // todo Step-7-a
        if(ELDSdk.isConnected()) {
            Intent intent = new Intent(getApplicationContext(), DataService.class);
            intent.setAction(DataService.ACTION_DISCONNECT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(getApplicationContext(), intent);
            } else {
                startService(intent);
            }
        }

        showProgressBar(false);
        showConnectBtn(true);
        showDisconnectBtn(false);
    }

    private void showConnectBtn(boolean show) {
        btnConnect.setEnabled(show);
        btnConnect.setClickable(show);
    }

    private void showDisconnectBtn(boolean show) {
        btnDisconnect.setEnabled(show);
        btnDisconnect.setClickable(show);
    }

    private void redirectToLogin() {
        finish();
        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
        startActivity(intent);
    }


    @Override
    public void onStateChanged(int state) {
        String message = ELDConstants.getMessage(state);
        Log.i(TAG, "onStateChanged: " + state + ":" + message);
        switch (state) {
            case ELDConstants.STATE_CONNECTED:
                showProgressBar(false);
                showConnectBtn(false);
                showDisconnectBtn(true);
                break;
            case ELDConstants.STATE_DEVICE_NOT_FOUND:
                showProgressBar(false);
                errorToast(message);
                break;
            case ELDConstants.STATE_CONNECTING:
                showProgressBar(true);
                break;
            case ELDConstants.STATE_DISCONNECTED:
                showProgressBar(false);
                errorToast(message);
                showConnectBtn(true);
                showDisconnectBtn(false);
                break;
            case ELDConstants.DEVICE_CLEAR_SUCCESS:
                redirectToLogin();
                break;
        }
    }

    @Override
    public void onAccessError(int error) {
        showProgressBar(false);
        String message = ELDConstants.getMessage(error);
        Log.i(TAG, "onAccessError: " + error + ":" + message);
        switch (error) {
            case ELDConstants.ERROR_PERMISSION_DENIED_TO_CONNECT:
                showProgressBar(false);
                errorToast(message);
                clearDevice();
                break;
            case ELDConstants.ERROR_BT_NOT_ENABLED:
                showBtEnableDialog();
                break;
            case ELDConstants.ERROR_LOCATION_NOT_ENABLED:
                showLocationEnableDialog();
                break;
            case ELDConstants.ERROR_LOCATION_PERMISSION_NOT_GRATED:
                askForPermission();
                break;
            case ELDConstants.ERROR_ACTIVITY_RECOGNITION_PERMISSION_NOT_GRATED:
                askForARPermission();
                break;
            case ELDConstants.ERROR_LOCATION_PERMISSION_IN_BACK_NOT_GRATED:
                askForLocationInBackgroundPermission();
                break;

        }
    }

    @Override
    public void onDeviceFound(String bluetoothDeviceName) {

    }

    @Override
    public void onDataReceived(String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtData.setText(data);
            }
        });
    }
}