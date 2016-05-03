package com.basmach.marshal.utils;

import android.util.Log;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.interfaces.IMarshalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarshalServiceProvider {

    // TODO
    private static final String MARSHAL_BASE_URL = "http://marshalweb.azurewebsites.net/api/";
    public static final String GET_ALL_COURSES = MARSHAL_BASE_URL + "courses";
    public static final String GET_ALL_MATERIALS = MARSHAL_BASE_URL + "materials";
    public static final String GET_ALL_RATINGS = MARSHAL_BASE_URL + "ratings";
    public static final String IMAGES_URL = MARSHAL_BASE_URL + "/images/";
    public static final String POST_RATING = MARSHAL_BASE_URL + "/ratings/";

    private static IMarshalService service;


    public static IMarshalService getInstance() {
        if (service != null) {
            return service;
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MARSHAL_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .registerTypeAdapter(Date.class, new JsonDateDeserializer())
                            .excludeFieldsWithoutExposeAnnotation()
                            .create()))
                    .build();

            service = retrofit.create(IMarshalService.class);
            return service;
        }
    }
}
