package com.basmapp.marshal.interfaces;

import com.basmapp.marshal.entities.AuthRequest;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.GcmRegistration;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.entities.Settings;
import com.basmapp.marshal.util.MarshalServiceProvider;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IMarshalService {
    // TODO
    @GET(MarshalServiceProvider.GET_ALL_COURSES)
    Call<List<Course>> getAllCourses();
    // TODO
    @GET(MarshalServiceProvider.GET_ALL_MATERIALS)
    Call<List<MaterialItem>> getAllMaterials();

    @GET(MarshalServiceProvider.GET_ALL_RATINGS)
    Call<List<Rating>> getAllRatings();

    @GET(MarshalServiceProvider.GET_ALL_RATINGS + "/{courseCode}")
    Call<List<Rating>> getCourseRatings(@Path("courseCode") String courseCode);

    @POST(MarshalServiceProvider.POST_RATING)
    Call<Rating> postRating(@Body Rating ratingObject);

    @PUT(MarshalServiceProvider.PUT_RATING)
    Call<Rating> updateRating(@Body Rating ratingObject);

    @DELETE(MarshalServiceProvider.DELETE_RATING + "{courseCode}/{userMailAddress}")
    Call<Rating> deleteRating(@Path("courseCode") String courseCode,
                              @Path("userMailAddress") String userMailAddress);

    @GET(MarshalServiceProvider.GET_ALL_MALSHAB_ITEMS)
    Call<List<MalshabItem>> getAllMalshabItems();

    //******** GCM ********//
    @POST (MarshalServiceProvider.POST_GCM_REGISTER_NEW_DEVICE)
    Call<GcmRegistration> gcmRegisterNewDevice(@Body GcmRegistration gcmRegistration);

    @PUT (MarshalServiceProvider.PUT_GCM_REGISTER_EXIST_DEVICE)
    Call<GcmRegistration> gcmRegisterExistDevice(@Body GcmRegistration gcmRegistration);

    @DELETE(MarshalServiceProvider.DELETE_GCM_UNREGISTER_DEVICE + "{hardwareId}")
    Call<Rating> deleteGcmRegistration(@Path("hardwareId") String hardwareId);

    @POST(MarshalServiceProvider.POST_SUBSCRIBE_COURSE + "{hardwareId}" + "\\" + "{courseCode}")
    Call<Void> subsribeCourse(@Path("hardwareId") String hardwareId, @Path("courseCode") String courseCode);

    @DELETE(MarshalServiceProvider.DELETE_UNSUBSCRIBE_COURSE + "{hardwareId}" + "\\" + "{courseCode}")
    Call<Void> unsubsribeCourse(@Path("hardwareId") String hardwareId, @Path("courseCode") String courseCode);

    // ******** Settings ********//
    @GET (MarshalServiceProvider.GET_SETTINGS)
    Call<Settings> getSettings();

    // ******** Auth ***********//
    @POST (MarshalServiceProvider.AUTH)
    Call<String> auth(@Body AuthRequest authRequest);

    @GET (MarshalServiceProvider.DASHBOARD)
    Call<String> testDashboard();
}
