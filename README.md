# EldSdk-Android-Sample

This guide gives information for the Tangerine-EldSdk integration with the third party android application.

Tangerine-EldSdk has been integrated into this project. You can go to the **TODO** section of the android studio project to check the Library usage steps to integrate it in your application.

Please refer to the ELDConstants class for error and state-related information.

## Library Usage

#### Step 1

To use it in your project, you need to declare the maven url inside the project level build.gradle.

```
allprojects {
   repositories {      
       maven {
           url "http://jfrog.tangerine.ai/artifactory/sdk-eld"
           credentials {
               username = "eld"
               password = "AP7kqQ9Yubehj25H6YkYbPr4Prv"
           }
       }
   }
}
```


#### Step 2

Open `build.gradle` inside module that you want to use the library and simply add a dependency.

```
dependencies {
   implementation 'ai.tangerine:eldsdk:1.0.0-alpha16'
}
```

#### Step 3

Open `Application` class file inside your app or create the Application class file and add the following code in `onCreate()` method.

Here, you have to pass the application context, App Name and Notification icon image resource.

```
public class SampleApplication extends Application {
   @Override
   public void onCreate() {
       super.onCreate();
       // todo step-3
       try {
            ELDSdk.init(getApplicationContext(), "ELD-SAMPLE", R.drawable.ic_notification);
        } catch (EldSdkIllegalStateException e) {
            e.printStackTrace();
        }
   }
}
```
#### Step 4

scan for the nearby vehicles for pairing using the following method:
Requires location permission to execute this method.
To connect to bluetooth and discover the nearby sense devices location permission is required.
Also, It requires Activity Recognition permission to auto connect to the ble Android 10 onwards.

you will get a callback on `void onDeviceFound(String bluetoothDeviceName)` for nearby devices
If timeout occurs for device scan then you will get callback on `onStateChanged(int state)` method with state as `ELDConstants.STATE_VEHICLE_NOT_FOUND`

```
        try {
            EldSdk.scanForDevices(new ELDListener() {
                @Override
                public void onStateChanged(int state) {
                    switch (state) {
                        case ELDConstants.STATE_VEHICLE_NOT_FOUND:
                            Toast.makeText(getApplicationContext(), "vehicle search time out - trt again", Toast.LENGTH_SHORT).show();
                            showScanBtn(true);
                            break;
                    }
                }
    
                @Override
                public void onAccessError(int i) {
                    showProgressBar(false);
                    switch (i) {
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
                    }
                }
    
                @Override
                public void onDeviceFound(String bluetoothDeviceName) {
                        runOnUiThread(() -> deviceAdapter.addDevice(bluetoothDeviceName));
                }
            });
        } catch (ELDSdkIllegalStateException | ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
```

#### Step 5

Save scanned device for auto connect. Once device is saved then you can skip the `EldSdk.scanForDevices` part.

```
        try {
            ELDSdk.saveDevice(device);
        } catch (ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
```


#### Step 6

Connect to the device once device is saved. You should run this method in foreground service

```
        try {
            ELDSdk.connect(new ELDListener() {
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
                        case ELDConstants.STATE_VEHICLE_NOT_FOUND:
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
                    }
                }
    
                @Override
                public void onAccessError(int i) {
                    showProgressBar(false);
                    switch (i) {
                        case ELDConstants.ERROR_PERMISSION_DENIED_TO_CONNECT:
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
                    }
                }
    
                @Override
                public void onDataReceived(String data) {
                    `// todo-6-c you can save the ELD data here`
                }
            });
```


#### Step 7

Disconnect the device using the following API.

```
        try {
            ELDSdk.disconnect(ELDListener);
        } catch (ELDSdkIllegalStateException | ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
        }
```

#### Step 8

Clears the existing saved device to connect to another device.

```
        try {
            ELDSdk.clearDevice(ELDListener);
        } catch (ELDSdkIllegalStateException | ELDSdkIllegalArgumentException e) {
            e.printStackTrace();
        }
```

#### Step 9 - AndroidManifest changes

Please add the following code in outer `application` tag.

```
    <permission android:name="ai.tangerine.eldsdk.permission"
        android:protectionLevel="signature" />
    <uses-permission android:name="ai.tangerine.eldsdk.permission" />

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

#### Step 10 - Broadcast details received from SDK related changes

You need to implement Broadcast receiver to get the broadcast for initialising the sdk and start connection process.
1. ELDConstants.BROADCAST_INIT_SDK :  This broadcast is triggered to initialise the sdk.
2. ELDConstants.BROADCAST_START_CONNECTION : This broadcast is triggered to start connection process in foreground service.

For more details refer `ai.tangerine.senseeldsdk.StatusReceiver.java` in sample application

Add receiver to AndroidManifest.xml file as below inside `application` tag.
```
        <receiver android:name=".StatusReceiver">
            <intent-filter>
                <action android:name="ai.tangerine.eldsdk.INIT_SDK" />
                <action android:name="ai.tangerine.eldsdk.START_CONNECTION" />
            </intent-filter>
        </receiver>
```

```
public class StatusReceiver extends BroadcastReceiver {

    private static final String TAG = "StatusReceiver";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
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

```

#### Get Device connection state

you can check if device is already connected with the Jido Sense device by using below api

```
        
            boolean state = ELDSdk.isConnected();
        
```

#### Get Device save status

Checks if device is being saved for connection.
You can check if device is already saved or not using below api

```
        
            boolean state = ELDSdk.isDeviceSaved();
        
```

#### Get saved device name

Gets the saved device name. If not saved it returns null or empty string

```
        
            String deviceName = ELDSdk.getSavedDevice();
        
```

#### Error handling

There are several error scenarios come when booking validation happens or while executing the command. User has to pass ``` ELDListener ``` object to handle error case.

Please find below list of errors that can occur during the booking validation.

1. ``` ELDConstants.ERROR_BT_NOT_ENABLED ```  :  This error occurs in case when user has not enabled the bluetooth.
2. ``` ELDConstants.ERROR_BT_NOT_AVAILABLE ```  :  This error occurs in case when device does not support bluetooth.
3. ``` ELDConstants.ERROR_LOCATION_NOT_ENABLED ``` : This error occurs in case when user has not enabled the Location.
4. ``` ELDConstants.ERROR_LOCATION_PERMISSION_NOT_GRATED ``` : This error occurs in case when user has not granted Location permission.
5. ``` ELDConstants.ERROR_ACTIVITY_RECOGNITION_PERMISSION_NOT_GRATED ``` : This error occurs in case when user has not granted Activity Recognition permission. Android version 10 onwards this permission is required for background ble scan.
6. ``` ELDConstants.ERROR_LOCATION_PERMISSION_IN_BACK_NOT_GRATED ``` : This error occurs in case when user has not granted Location permission in background. Android version 10 onwards this permission is required for background ble scan.
7. ``` ELDConstants.ERROR_PERMISSION_DENIED_TO_CONNECT ``` : This error occurs when another app is already connected to vehicle/device.


#### State information while connecting to the device or executing the lock/unlock feature

Please find below list of state that can occur during the connection and execution of lock/unlock features.

1. ``` ELDConstants.STATE_CONNECTING ```  :  App starts connecting to the device.
2. ``` ELDConstants.STATE_CONNECTED ```  :  App is connected to device.
3. ``` ELDConstants.STATE_DISCONNECTED ``` : App is disconnected to device because of range.
4. ``` ELDConstants.STATE_DEVICE_NOT_FOUND ``` : When app is unable to connect to the device after ~25 seconds of time or app is not near to the device.
5. ``` ELDConstants.DEVICE_CLEAR_SUCCESS ``` : When device is cleared successfully.


#### Exception information while connecting to the device or executing the lock/unlock feature

1. ``` ELDConstants.EXCEPTION_SDK_NOT_INIT ```  :  When SDK is not initialised.
2. ``` ELDConstants.EXCEPTION_ELD_LISTENER ```  :  When ``` ELDListener ``` is null.
3. ``` ELDConstants.EXCEPTION_CONTEXT ```  :  When ``` Context ``` is null.
4. ``` ELDConstants.EXCEPTION_DEVICE_NAME_NULL ```  :  When ``` device name ``` is null or empty while ``` saveDevice() ```.
5. ``` ELDConstants.EXCEPTION_DEVICE_NOT_SAVED ```  :  When ``` device name ``` is not saved and trying to execute the ``` connect() ``` .


#### Note
1. We recommend to run the ```connect()``` method once you use ```scanDevice()``` and save the device with ```saveDevice()```
2. We recommend to run the ```connect()``` method in foreground service as implemented in sample app. So that once sdk detects that you are inside the vehicle it will try to connect in the background and get the ELD data in background.

For more detailed implementation please refer the sample app code.

