package com.basmach.marshal.utils;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.interfaces.IMarshalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarshalServiceProvider {

    // TODO
    private static final String MARSHAL_BASE_URL = "";
    public static final String GET_ALL_COURSES = "";
    public static final String GET_ALL_MATERIALS = "";

    private static IMarshalService service;

    private static IMarshalService getInstance() {
        if (service != null) {
            return service;
        } else {
            Gson gson = new GsonBuilder()
                    .setDateFormat(DateHelper.DATE_FORMAT)
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MARSHAL_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            service = retrofit.create(IMarshalService.class);
            return service;
        }
    }

    public static void getAllCourses(Callback<List<Course>> callback) {
        getInstance().getAllCoureses().enqueue(callback);
    }

    public static void setGetAllMaterials(Callback<List<MaterialItem>> callback) {
        getInstance().getAllMaterials().enqueue(callback);
    }
}
