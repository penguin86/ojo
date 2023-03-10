package it.danieleverducci.ojo.ui;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private ActivitySettingsBinding binding;
    private NavController navController;
    private OnBackButtonPressedListener onBackButtonPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_settings);
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

    public void navigateToFragment(int actionId) {
        navigateToFragment(actionId, null);
    }

    public void navigateToFragment(int actionId, Bundle bundle) {
        if (navController == null) {
            Log.e(TAG, "Not initialized");
            return;
        }

        try {
            if (bundle != null)
                navController.navigate(actionId, bundle);
            else
                navController.navigate(actionId);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to navigate to fragment: " + e.getMessage());
        }
    }

    public void toggleRotationEnabledSetting() {
        SharedPreferencesManager.toggleRotationEnabled(this);
    }

    public boolean getRotationEnabledSetting() {
        return SharedPreferencesManager.loadRotationEnabled(this);
    }
}