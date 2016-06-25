package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.MalshabItem;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.adapters.MalshabCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

public class MalshabFragment extends Fragment {

    List<MalshabItem> mMalshabItems;
    MalshabCoursesAdapter mAdapter;

    GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_malshab, container, false);
        setHasOptionsMenu(true);

        mGridView = (GridView)rootView.findViewById(R.id.gridview);

        if (mMalshabItems == null) {
            MalshabItem.getAllInBackground(DBConstants.COL_TITLE, MalshabItem.class, getActivity(),
                    true, new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {
                            if (data != null) {
                                mMalshabItems = (List)data;
                            } else {
                                mMalshabItems = new ArrayList<>();
                            }

                            showData();
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
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
