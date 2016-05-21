package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.basmach.marshal.R;
import com.basmach.marshal.ui.adapters.MalshabCoursesAdapter;

public class MalshabFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_malshab, container, false);
        setHasOptionsMenu(true);

        GridView gridView = (GridView)rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new MalshabCoursesAdapter(getActivity()));

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_main_searchView).setVisible(false);
        menu.findItem(R.id.menu_main_refresh).setVisible(false);
    }
}
