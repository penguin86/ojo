package it.danieleverducci.ojo.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private OnBackButtonPressedListener onBackButtonPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Interface can go below notches
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fab.setOnClickListener(view -> openSettings());
    }

    @Override
    protected void onStart() {
        boolean rotationEnabledSetting = SharedPreferencesManager.loadRotationEnabled(this);
        this.setRequestedOrientation(rotationEnabledSetting ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onStart();
    }

    public void setOnBackButtonPressedListener(OnBackButtonPressedListener onBackButtonPressedListener) {
        this.onBackButtonPressedListener = onBackButtonPressedListener;
    }

    @Override
    public void onBackPressed() {
        if (this.onBackButtonPressedListener != null && this.onBackButtonPressedListener.onBackPressed())
            return;
        super.onBackPressed();
    }

    private void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

}