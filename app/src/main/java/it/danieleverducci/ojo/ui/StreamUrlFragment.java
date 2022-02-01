package it.danieleverducci.ojo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.CamerasSettings;
import it.danieleverducci.ojo.databinding.FragmentAddStreamBinding;
import it.danieleverducci.ojo.entities.Camera;

public class StreamUrlFragment extends Fragment {
    public static final String ARG_CAMERA = "arg_camera";

    private FragmentAddStreamBinding binding;
    private CamerasSettings camerasSettings;
    private Integer selectedCamera = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load existing settings (if any)
        camerasSettings = CamerasSettings.fromDisk(getContext());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddStreamBinding.inflate(inflater, container, false);

        // If passed an url, fill the details
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_CAMERA)) {
            this.selectedCamera = args.getInt(ARG_CAMERA);

            Camera c = camerasSettings.getCameras().get(this.selectedCamera);
            binding.streamName.setText(c.getName());
            binding.streamName.setHint(getContext().getString(R.string.stream_list_default_camera_name).replace("{camNo}", (this.selectedCamera+1)+""));
            binding.streamUrl.setText(c.getRtspUrl());
        }

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check the field is filled
                String url = binding.streamUrl.getText().toString();
                if (!(url.startsWith("rtsp://") || url.startsWith("http://"))) {
                    Snackbar.make(view, R.string.add_stream_invalid_url, Snackbar.LENGTH_LONG)
                        .setAction(R.string.add_stream_invalid_url_dismiss, null).show();
                    return;
                }

                // Name can be empty
                String name = binding.streamName.getText().toString();

                if (StreamUrlFragment.this.selectedCamera != null) {
                    // Update camera
                    Camera c = camerasSettings.getCameras().get(StreamUrlFragment.this.selectedCamera);
                    c.setName(name);
                    c.setRtspUrl(url);
                } else {
                    // Add stream to list
                    camerasSettings.addCamera(new Camera(name, url));
                }

                // Save
                if (!camerasSettings.save()) {
                    Snackbar.make(view, R.string.add_stream_error_saving, Snackbar.LENGTH_LONG).show();
                    return;
                }

                // Back to first fragment
                NavHostFragment.findNavController(StreamUrlFragment.this)
                        .popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}