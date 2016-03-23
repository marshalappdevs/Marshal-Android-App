package com.basmach.marshal.utils;

import android.content.Context;

import com.basmach.marshal.entities.MaterialItem;

import java.util.ArrayList;

/**
 * Created by Ido on 3/23/2016.
 */
public class MockDataProvider {

    public static ArrayList<MaterialItem> getAllMaterials(Context context) {
        ArrayList<MaterialItem> tempList = new ArrayList<>();

        MaterialItem m1 = new MaterialItem(context);
        MaterialItem m2 = new MaterialItem(context);
        MaterialItem m3 = new MaterialItem(context);
        MaterialItem m4 = new MaterialItem(context);
        MaterialItem m5 = new MaterialItem(context);
        MaterialItem m6 = new MaterialItem(context);
        MaterialItem m7 = new MaterialItem(context);

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
