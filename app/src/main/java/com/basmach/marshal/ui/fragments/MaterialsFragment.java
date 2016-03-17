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
import com.basmach.marshal.ui.utils.MaterialsRecyclerAdapter;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MaterialsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private FloatingActionButton mFabPlus;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private MaterialsRecyclerAdapter mAdapter;
    private ArrayList<MaterialItem> mMaterialsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_materials, container, false);

        mMaterialsList = getAllMockMaterials();

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.materials_progressBar);
        mFabPlus = (FloatingActionButton) rootView.findViewById(R.id.materials_fab_plus);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.materials_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mAdapter = new MaterialsRecyclerAdapter(getActivity(), mMaterialsList);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setAdapter(mAdapter);

        return rootView;
    }

    private ArrayList<MaterialItem> getAllMockMaterials() {
        ArrayList<MaterialItem> tempList = new ArrayList<>();

        tempList.add(new MaterialItem("http://stackoverflow.com/questions/28525112/android-recyclerview-vs-listview-with-viewholder"));
        tempList.add(new MaterialItem("https://github.com/LeonardoCardoso/Android-Link-Preview"));
        tempList.add(new MaterialItem("https://www.youtube.com/watch?v=golN1j4rAug"));
        tempList.add(new MaterialItem("https://www.instagram.com/p/BCpzIvrnjCQ/?taken-by=ido.amram"));
        tempList.add(new MaterialItem("http://www.sport5.co.il/HTML/Articles/Article.398.210717.html"));
        tempList.add(new MaterialItem("https://www.wetsuitcentre.co.uk/firewire-baked-potato-timber-tek-surfboard-5ft-5.html"));
        tempList.add(new MaterialItem("http://veg.anonymous.org.il/art4.html"));

        return tempList;
    }
}
