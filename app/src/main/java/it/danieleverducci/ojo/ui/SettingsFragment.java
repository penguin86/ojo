package it.danieleverducci.ojo.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.databinding.FragmentSettingsItemListBinding;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.adapters.SettingsRecyclerViewAdapter;
import it.danieleverducci.ojo.utils.ItemMoveCallback;

/**
 * A fragment representing a list of Items.
 */
public class SettingsFragment extends Fragment {
    private static final String EXPORT_FILE_NAME = "ojo-settings.bin";

    private FragmentSettingsItemListBinding binding;
    private Settings settings;
    private ItemTouchHelper touchHelper;

    // Activity result launchers for file export/import
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CreateDocument lets the user create a new file and returns a writable
        // Uri (GetContent would return a read-only one)
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsItemListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Setup toolbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.settingsToolbar.getOverflowIcon().setTint(Color.WHITE);
        }
        binding.settingsToolbar.inflateMenu(R.menu.settings_menu);
        MenuItem rotMenuItem = binding.settingsToolbar.getMenu().findItem(R.id.menuitem_allow_rotation);
        rotMenuItem.setTitle(((SettingsActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);

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
                        item.setTitle(((SettingsActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);
                        return true;
                    case R.id.menuitem_info:
                        ((SettingsActivity)getActivity()).navigateToFragment(R.id.action_SettingsToInfoFragment);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        setupCameraList();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save cameras
        List<Camera> cams = ((SettingsRecyclerViewAdapter)binding.list.getAdapter()).getItems();
        this.settings.setCameras(cams);
        this.settings.save();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * (Re)loads the settings from disk and wires up the camera list:
     * adapter, drag&drop and click listeners.
     */
    private void setupCameraList() {
        settings = Settings.fromDisk(getContext());
        List<Camera> cams = settings.getCameras();

        // Set the adapter
        RecyclerView recyclerView = binding.list;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(cams);
        // Detach the previous ItemTouchHelper, if any: they accumulate as
        // touch listeners on the RecyclerView otherwise
        if (touchHelper != null) {
            touchHelper.attachToRecyclerView(null);
        }
        touchHelper = new ItemTouchHelper(new ItemMoveCallback(adapter));
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
    }

    private void exportSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // ACTION_CREATE_DOCUMENT requires API 19
            Toast.makeText(requireContext(), R.string.settings_export_unsupported, Toast.LENGTH_SHORT).show();
            return;
        }
        // Launch file creator for exporting settings
        exportLauncher.launch(EXPORT_FILE_NAME);
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
                Toast.makeText(requireContext(), R.string.settings_export_nothing, Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy the file to the selected Uri
            try (InputStream inputStream = new FileInputStream(internalFile);
                 OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {

                if (outputStream == null) {
                    Toast.makeText(requireContext(), R.string.settings_export_write_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                copyStream(inputStream, outputStream);

                Toast.makeText(requireContext(), R.string.settings_export_success, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | SecurityException e) {
            Toast.makeText(requireContext(), getString(R.string.settings_export_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void importSettingsFromUri(Uri uri) {
        // Copy to a temporary file first: the current settings must not be
        // touched until the selected file proves to be a valid settings file
        File tempFile = new File(requireContext().getCacheDir(), Settings.getFileName());
        try {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {

                if (inputStream == null) {
                    Toast.makeText(requireContext(), R.string.settings_import_read_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                copyStream(inputStream, outputStream);
            }

            // Validate before overwriting the current settings
            if (Settings.fromFile(tempFile) == null) {
                Toast.makeText(requireContext(), R.string.settings_import_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Replace the settings file in internal storage
            File internalFile = new File(requireContext().getFilesDir(), Settings.getFileName());
            try (InputStream inputStream = new FileInputStream(tempFile);
                 OutputStream outputStream = new FileOutputStream(internalFile)) {
                copyStream(inputStream, outputStream);
            }

            // Reload the settings and refresh the UI
            setupCameraList();

            Toast.makeText(requireContext(), R.string.settings_import_success, Toast.LENGTH_SHORT).show();
        } catch (IOException | SecurityException e) {
            Toast.makeText(requireContext(), getString(R.string.settings_import_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        } finally {
            tempFile.delete();
        }
    }

    private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
    }
}
