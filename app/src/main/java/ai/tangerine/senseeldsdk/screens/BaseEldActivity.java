package ai.tangerine.senseeldsdk.screens;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ai.tangerine.eldsdk.ELDListener;
import ai.tangerine.senseeldsdk.DataService;
import ai.tangerine.senseeldsdk.R;

public abstract class BaseEldActivity extends AppCompatActivity implements ELDListener {

    private static final String TAG = "BaseEldActivity";
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    static final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 98;
    static final int MY_PERMISSIONS_REQUEST_LOCATION_IN_BACK = 97;
    final int REQUEST_ENABLE_BT = 1000;

    Dialog progressDialog;

    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    //-----------------------------------------------------------
    // BT related
    //-----------------------------------------------------------

    public void showBtEnableDialog() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(getApplicationContext(), R.string.bt_enabled, Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //-----------------------------------------------------------
    // Location permission related
    //-----------------------------------------------------------

    public void askForPermission() {
        if (ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                    .setTitle(R.string.title_location_permission)
                    .setMessage(R.string.text_location_permission)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(BaseEldActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .create()
                    .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void askForLocationInBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                    .setTitle(R.string.title_location_permission_in_back)
                    .setMessage(R.string.text_location_permission_in_back)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(BaseEldActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION_IN_BACK);
                        }
                    })
                    .create()
                    .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION_IN_BACK);
            }
        }
    }

    public void askForARPermission() {
        if (ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACTIVITY_RECOGNITION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                    .setTitle(R.string.title_activity_permission)
                    .setMessage(R.string.text_activity_permission)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(BaseEldActivity.this,
                                new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
                        }
                    })
                    .create()
                    .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (MY_PERMISSIONS_REQUEST_LOCATION == requestCode
            || MY_PERMISSIONS_REQUEST_LOCATION_IN_BACK == requestCode
            || MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                errorToast(R.string.permission_granted);
            } else {
                errorToast(R.string.permission_not_granted);
                openAppSettings();
            }
        }
    }

    public void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------
    // Location enable related
    //-----------------------------------------------------------

    public void showLocationEnableDialog() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.dialog_location_enable_title)
            .setPositiveButton(R.string.dialog_location_enable_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    navigateToLocationSettings();
                    dialogInterface.dismiss();
                }
            })
            .setNegativeButton(R.string.dialog_location_enable_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .setCancelable(false)
            .show();
    }

    private void navigateToLocationSettings() {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(myIntent);
    }

    //-----------------------------------------------------------
    // Other UI
    //-----------------------------------------------------------

    public void errorToast(final @StringRes int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void errorToast(final String res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showProgressBar(boolean show) {
        if (show) {
            if (progressDialog == null) {
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(BaseEldActivity.this);
                View view = getLayoutInflater().inflate(R.layout.progress, null);
                alert.setView(view);
                progressDialog = alert.create();
                if (progressDialog.getWindow() != null) {
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                }
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing() && !this.isFinishing()) {
                progressDialog.dismiss();
            }
        }
    }

    //-----------------------------------------------------------
    // Bound service Messenger related
    //-----------------------------------------------------------

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataService.MSG_ERROR:
                    Log.i(TAG, "MSG_ERROR: " + msg.arg1);
                    onAccessError(msg.arg1);
                    break;
                case DataService.MSG_STATE:
                    Log.i(TAG, "MSG_STATE: " + msg.arg1);
                    onStateChanged(msg.arg1);
                    break;
                case DataService.MSG_DATA:
                    Log.i(TAG, "MSG_DATA: " + msg.obj.toString());
                    onDataReceived(msg.obj.toString());
                    break;
                case DataService.MSG_DEVICE:
                    Log.i(TAG, "MSG_DEVICE: " + msg.obj.toString());
                    onDeviceFound(msg.obj.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, DataService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
               e.printStackTrace();
            }
            Log.i(TAG, "service bound");
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.i(TAG, "service un bound");
        }
    };

    void doBindService() {
        if(!mIsBound) {
            bindService(new Intent(BaseEldActivity.this,
                DataService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, DataService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(mConnection);
            mIsBound = false;
            Log.i(TAG, "Unbinding.");
        }
    }

    public void sendCommandToService(int command) {
        if (mService != null) {
            try {
                Message msg = Message.obtain(null, command);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
