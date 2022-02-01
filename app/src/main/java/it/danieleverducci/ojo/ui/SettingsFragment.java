package it.danieleverducci.ojo.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    private FragmentSettingsItemListBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsItemListBinding.inflate(inflater, container, false);

        // Load cameras
        Settings settings = Settings.fromDisk(getContext());
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
                ((MainActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl, b);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.settingsToolbar.inflateMenu(R.menu.settings_menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuitem_add_camera) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}