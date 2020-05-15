package ai.tangerine.senseeldsdk.screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ai.tangerine.eldsdk.ELDConstants;
import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.eldsdk.ELDSdkIllegalArgumentException;
import ai.tangerine.eldsdk.ELDSdkIllegalStateException;
import ai.tangerine.senseeldsdk.DataService;
import ai.tangerine.senseeldsdk.R;


public class ScanActivity extends BaseEldActivity implements DeviceAdapter.OnDeviceClickListener {

    private RecyclerView recyclerView;
    private AppCompatButton btnScanVehicle;
    private static final String TAG = "ScanActivity";

    DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        btnScanVehicle = findViewById(R.id.scan_vehicle);

        btnScanVehicle.setOnClickListener(view -> {
            showScanBtn(false);
            startScanning();
        });

        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(getApplicationContext(), this);
        recyclerView.setAdapter(deviceAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }


    @SuppressLint("MissingPermission")
    private void startScanning() {
        // todo step-4-a
        // scan for nearby sense devices. Requires location permission to execute this method.
        // To connect to bluetooth and discover the nearby sense devices location permission is required.
        // Also, It requires Activity Recognition permission to auto connect to the ble android 10 onwards
        sendCommandToService(DataService.MSG_SCAN_FOR_DEVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceClicked(String device) {
        // todo step-5
        // Save scanned device for auto connect.
        // Once device is saved then you can skip the EldSdk.scanForDevices part.
        try {
            ELDSdk.saveDevice(device);
        } catch (ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

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

    private void showScanBtn(boolean show) {
        btnScanVehicle.setClickable(show);
        btnScanVehicle.setEnabled(show);
    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
            case ELDConstants.STATE_CONNECTED:
                showProgressBar(false);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case ELDConstants.STATE_CONNECTING:
                showProgressBar(true);
                break;
            case ELDConstants.STATE_DISCONNECTED:
                showProgressBar(false);
                askForPermission();
                break;
            case ELDConstants.STATE_DEVICE_NOT_FOUND:
                Toast.makeText(getApplicationContext(), ELDConstants.getMessage(state), Toast.LENGTH_SHORT).show();
                showScanBtn(true);
                break;
        }
    }

    @Override
    public void onAccessError(int error) {
        showProgressBar(false);
        showScanBtn(true);
        switch (error) {
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
        runOnUiThread(() -> deviceAdapter.addDevice(bluetoothDeviceName));
    }

    @Override
    public void onDataReceived(String data) {

    }
}
