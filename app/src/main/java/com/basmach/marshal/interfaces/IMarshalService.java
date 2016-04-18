package com.basmach.marshal.interfaces;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.utils.MarshalServiceProvider;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface IMarshalService {
    // TODO
    @GET(MarshalServiceProvider.GET_ALL_COURSES)
    Call<List<Course>> getAllCoureses();
    // TODO
    @GET(MarshalServiceProvider.GET_ALL_MATERIALS)
    Call<List<MaterialItem>> getAllMaterials();
}
