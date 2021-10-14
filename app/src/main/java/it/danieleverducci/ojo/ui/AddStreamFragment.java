package it.danieleverducci.ojo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.databinding.FragmentAddStreamBinding;
import it.danieleverducci.ojo.entities.Camera;

public class AddStreamFragment extends Fragment {

    private FragmentAddStreamBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddStreamBinding.inflate(inflater, container, false);
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

                // Load existing settings (if any)
                Settings settings = Settings.fromDisk(getContext());
                // Add stream to list
                settings.addCamera(new Camera("", url));
                // Save
                if (!settings.save()) {
                    Snackbar.make(view, R.string.add_stream_error_saving, Snackbar.LENGTH_LONG).show();
                    return;
                }

                // Back to first fragment
                NavHostFragment.findNavController(AddStreamFragment.this)
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