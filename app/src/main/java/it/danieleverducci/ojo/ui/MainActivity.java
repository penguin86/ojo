package it.danieleverducci.ojo.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private boolean rotationEnabledSetting;
    private OnBackButtonPressedListener onBackButtonPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotationEnabledSetting = SharedPreferencesManager.loadRotationEnabled(this);
        this.setRequestedOrientation(this.rotationEnabledSetting ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show FAB only on first fragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.HomeFragment)
                binding.fab.show();
            else
                binding.fab.hide();
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToFragment(R.id.action_homeToSettings);
            }
        });
    }

    public void setOnBackButtonPressedListener(OnBackButtonPressedListener onBackButtonPressedListener) {
        this.onBackButtonPressedListener = onBackButtonPressedListener;
    }

    @Override
    public void onBackPressed() {
        if (this.onBackButtonPressedListener != null && this.onBackButtonPressedListener.onBackPressed())
            return;
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
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

    public boolean getRotationEnabledSetting() {
        return this.rotationEnabledSetting;
    }

    public void toggleRotationEnabledSetting() {
        this.rotationEnabledSetting = !this.rotationEnabledSetting;
        this.setRequestedOrientation(this.rotationEnabledSetting ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}