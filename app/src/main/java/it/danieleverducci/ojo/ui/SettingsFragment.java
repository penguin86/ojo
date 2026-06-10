package it.danieleverducci.ojo.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.FragmentSettingsItemListBinding;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.adapters.SettingsRecyclerViewAdapter;
import it.danieleverducci.ojo.utils.ItemMoveCallback;

/**
 * A fragment representing a list of Items.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsItemListBinding binding;
    private Settings settings;

    // Activity result launchers for file export/import
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String> importLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsItemListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            // Setup toolbar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.settingsToolbar.getOverflowIcon().setTint(Color.WHITE);
            }
            binding.settingsToolbar.inflateMenu(R.menu.settings_menu);
            MenuItem rotMenuItem = binding.settingsToolbar.getMenu().findItem(R.id.menuitem_allow_rotation);
            rotMenuItem.setTitle(((SettingsActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);

            // Initialize activity result launchers for export/import functionality
            exportLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                exportSettingsToUri(uri);
                            }
                        }
                    });

            importLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                importSettingsFromUri(uri);
                            }
                        }
                    });

            // Register for item click
            binding.settingsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menuitem_add_camera:
                            ((SettingsActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl);
                            return true;
                        case R.id.menuitem_export_config:
                            exportSettings();
                            return true;
                        case R.id.menuitem_import_config:
                            importSettings();
                            return true;
                        case R.id.menuitem_allow_rotation:
                            ((SettingsActivity)getActivity()).toggleRotationEnabledSetting();
                            SharedPreferencesManager.saveRotationEnabled(getContext(), ((SettingsActivity)getActivity()).getRotationEnabledSetting());
                            item.setTitle(((SettingsActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);
                            return true;
                        case R.id.menuitem_info:
                            ((SettingsActivity)getActivity()).navigateToFragment(R.id.action_SettingsToInfoFragment);
                            return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e("SettingsFragment", "Exception in onViewCreated", e);
            throw e;
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();

            // Load cameras
            settings = Settings.fromDisk(getContext());
            List<Camera> cams = settings.getCameras();

            // Set the adapter
            RecyclerView recyclerView = binding.list;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(cams);
            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
            adapter.setOnDragListener(touchHelper::startDrag);
            recyclerView.setAdapter(adapter);
            // Onclick listener
            adapter.setOnClickListener(new SettingsRecyclerViewAdapter.OnClickListener() {
                @Override
                public void onItemClick(int pos) {
                    Bundle b = new Bundle();
                    b.putInt(StreamUrlFragment.ARG_CAMERA, pos);
                    ((SettingsActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl, b);
                }
            });
        } catch (Exception e) {
            Log.e("SettingsFragment", "Exception in onResume", e);
            throw e;
        }
    }

    @Override
    public void onPause() {
        try {
            super.onPause();

            // Save cameras
            List<Camera> cams = ((SettingsRecyclerViewAdapter)binding.list.getAdapter()).getItems();
            this.settings.setCameras(cams);
            this.settings.save();
        } catch (Exception e) {
            Log.e("SettingsFragment", "Exception in onPause", e);
            throw e;
        }
    }

    private void exportSettings() {
        // Launch file picker for exporting settings
        exportLauncher.launch("*/*");
    }

    private void importSettings() {
        // Launch file picker for importing settings
        importLauncher.launch("*/*");
    }

    private void exportSettingsToUri(Uri uri) {
        try {
            // Get the settings file from internal storage
            File internalFile = new File(requireContext().getFilesDir(), Settings.getFileName());
            if (!internalFile.exists()) {
                Toast.makeText(requireContext(), "No settings found to export", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy the file to the selected Uri
            try (InputStream inputStream = new FileInputStream(internalFile);
                 OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {

                if (outputStream == null) {
                    Toast.makeText(requireContext(), "Unable to open file for writing", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                Toast.makeText(requireContext(), "Settings exported successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error exporting settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void importSettingsFromUri(Uri uri) {
        try {
            // Get the settings file from internal storage (destination)
            File internalFile = new File(requireContext().getFilesDir(), Settings.getFileName());

            // Copy the selected file to internal storage
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(internalFile)) {

                if (inputStream == null) {
                    Toast.makeText(requireContext(), "Unable to open file for reading", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // Reload the settings and refresh the UI
                settings = Settings.fromDisk(requireContext());
                List<Camera> cams = settings.getCameras();
                RecyclerView recyclerView = binding.list;
                SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(cams);
                recyclerView.setAdapter(adapter);

                Toast.makeText(requireContext(), "Settings imported successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error importing settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}