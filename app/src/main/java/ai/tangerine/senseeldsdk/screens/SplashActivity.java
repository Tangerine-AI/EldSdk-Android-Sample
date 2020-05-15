package ai.tangerine.senseeldsdk.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.senseeldsdk.R;
import ai.tangerine.senseeldsdk.screens.MainActivity;
import ai.tangerine.senseeldsdk.screens.ScanActivity;


public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_PERIOD = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        init();
    }

    private void init() {
        if (ELDSdk.isDeviceSaved()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_PERIOD);
        }
    }
}
