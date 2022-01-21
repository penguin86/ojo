package it.danieleverducci.ojo.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.adapters.SettingsRecyclerViewAdapter;
import it.danieleverducci.ojo.utils.ItemMoveCallback;

/**
 * A fragment representing a list of Items.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_item_list, container, false);

        // Load cameras
        Settings settings = Settings.fromDisk(getContext());
        List<Camera> cams = settings.getCameras();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(cams);
            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
            adapter.setOnDragListener(touchHelper::startDrag);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }
}