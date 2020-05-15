package ai.tangerine.senseeldsdk;

import android.app.Application;

import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.eldsdk.ELDSdkIllegalStateException;


public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // todo step-3
        try {
            ELDSdk.init(getApplicationContext(), "ELD-SAMPLE", R.drawable.ic_notification);
        } catch (ELDSdkIllegalStateException e) {
            e.printStackTrace();
        }
    }
}
