package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.utils.MaterialsRecyclerAdapter;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MaterialsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private MaterialsRecyclerAdapter mAdapter;
    private ArrayList<MaterialItem> mMaterialsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_materials, container, false);

//        mMaterialsList = getAllMockMaterials();

        MaterialItem.getAllInBackground(DBConstants.COL_TITLE, MaterialItem.class, getActivity(),
                true, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        mMaterialsList = new ArrayList<>();
                        for(Object item:data) {
                            Log.i("GET MATERIALS "," ITEM: " + ((MaterialItem)item).getTitle());
                            mMaterialsList.add((MaterialItem)item);
                            showData();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("GET MATERIALS "," ERROR");
                    }
                });
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.materials_progressBar);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.materials_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    private void showData() {
        mAdapter = new MaterialsRecyclerAdapter(getActivity(), mMaterialsList);
        mRecycler.setAdapter(mAdapter);
    }

    private ArrayList<MaterialItem> getAllMockMaterials() {
        ArrayList<MaterialItem> tempList = new ArrayList<>();

        MaterialItem m1 = new MaterialItem(getActivity());
        MaterialItem m2 = new MaterialItem(getActivity());
        MaterialItem m3 = new MaterialItem(getActivity());
        MaterialItem m4 = new MaterialItem(getActivity());
        MaterialItem m5 = new MaterialItem(getActivity());
        MaterialItem m6 = new MaterialItem(getActivity());
        MaterialItem m7 = new MaterialItem(getActivity());

        m1.setUrl("http://stackoverflow.com/questions/28525112/android-recyclerview-vs-listview-with-viewholder");
        m2.setUrl("https://github.com/LeonardoCardoso/Android-Link-Preview");
        m3.setUrl("https://www.youtube.com/watch?v=golN1j4rAug");
        m4.setUrl("https://www.instagram.com/p/BCpzIvrnjCQ/?taken-by=ido.amram");
        m5.setUrl("http://www.sport5.co.il/HTML/Articles/Article.398.210717.html");
        m6.setUrl("https://www.wetsuitcentre.co.uk/firewire-baked-potato-timber-tek-surfboard-5ft-5.html");
        m7.setUrl("http://veg.anonymous.org.il/art4.html");

        tempList.add(m1);
        tempList.add(m2);
        tempList.add(m3);
        tempList.add(m4);
        tempList.add(m5);
        tempList.add(m6);
        tempList.add(m7);

        return tempList;
    }
}
