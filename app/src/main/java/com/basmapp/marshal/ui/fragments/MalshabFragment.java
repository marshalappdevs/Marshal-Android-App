package com.basmapp.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.adapters.MalshabCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

public class MalshabFragment extends Fragment {

    ArrayList<MalshabItem> mMalshabItems;
    MalshabCoursesAdapter mAdapter;

    GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_malshab, container, false);

        setHasOptionsMenu(true);

        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        if (mMalshabItems == null) {
            if (MainActivity.sMalshabItems == null) {
                MalshabItem.findAllInBackground(DBConstants.COL_TITLE, MalshabItem.class, getActivity(),
                        true, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                if (data != null) {
                                    try {
                                        mMalshabItems = (ArrayList) data;
                                        MainActivity.sMalshabItems = mMalshabItems;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mMalshabItems = new ArrayList<>();
                                    }
                                } else {
                                    mMalshabItems = new ArrayList<>();
                                }

                                showData();
                            }

                            @Override
                            public void onError(String error) {
                                mMalshabItems = new ArrayList<>();
                                showData();
                            }
                        });
            } else {
                mMalshabItems = new ArrayList<>(MainActivity.sMalshabItems);
                showData();
            }
        } else {
            showData();
        }

        return rootView;
    }

    private void showData() {
        if (mAdapter == null)
            mAdapter = new MalshabCoursesAdapter(getActivity(), mMalshabItems);

        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_main_searchView).setVisible(false);
    }
}
