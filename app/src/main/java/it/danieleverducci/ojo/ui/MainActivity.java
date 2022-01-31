package it.danieleverducci.ojo.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show FAB only on first fragment
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
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

    public void navigateToFragment(int actionId) {
        if (navController == null) {
            Log.e(TAG, "Not initialized");
            return;
        }

        navController.navigate(actionId);
    }
}