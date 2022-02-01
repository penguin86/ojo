package it.danieleverducci.ojo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import it.danieleverducci.ojo.databinding.FragmentInfoBinding;


public class InfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return FragmentInfoBinding.inflate(inflater, container, false).getRoot();
    }
}
