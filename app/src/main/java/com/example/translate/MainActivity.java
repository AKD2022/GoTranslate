package com.example.translate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.translate.AlwaysAvailablePages.DownloadLanguageTranslatePackages;
import com.example.translate.AlwaysAvailablePages.ImageTranslateActivity;
import com.example.translate.AlwaysAvailablePages.TextTranslateActivity;
import com.example.translate.AlwaysAvailablePages.VoiceTranslateActivity;
import com.example.translate.FirstTimeUserInstructions.InstructionsPage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_LAUNCH_KEY = "first_launch";


    /* Home Screen Buttons */
    MaterialButton toImage, toText, toVoice, toDownload;
    FloatingActionButton ft;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if this is the first launch
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstLaunch = settings.getBoolean(FIRST_LAUNCH_KEY, true);

        if (firstLaunch) {
            // Launch the InstructionsPage activity
            Intent intent = new Intent(this, InstructionsPage.class);
            startActivity(intent);
            finish();

            // Update the shared preference to indicate the app has been launched
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_LAUNCH_KEY, false);
            editor.apply();
            return;
        }

        // Regular launch if not the first time
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Initialize Home Screen Buttons */
        toImage = findViewById(R.id.toImage);
        toText = findViewById(R.id.toText);
        toVoice = findViewById(R.id.toVoice);
        toDownload = findViewById(R.id.downloadLanguages);
        ft = findViewById(R.id.ft);

        /* Home Screen Button Navigation */
        toImage.setOnClickListener(view -> {
            toImage();
        });

        toText.setOnClickListener(view -> {
            toText();
        });

        toVoice.setOnClickListener(view -> {
            toVoice();
        });

        toDownload.setOnClickListener(view -> {
            toDownload();
        });

        ft.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, InstructionsPage.class));
            finish();
        });
    }

    void toImage() {
        startActivity(new Intent(MainActivity.this, ImageTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toText() {
        startActivity(new Intent(MainActivity.this, TextTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(MainActivity.this, VoiceTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(MainActivity.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
