package com.basmapp.marshal.interfaces;

import com.basmapp.marshal.entities.AuthRequest;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.FcmRegistration;
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
import retrofit2.http.HTTP;
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

//    @GET(MarshalServiceProvider.GET_ALL_RATINGS)
//    Call<List<Rating>> getAllRatings();

//    @GET(MarshalServiceProvider.GET_ALL_RATINGS + "/{courseCode}")
//    Call<List<Rating>> getCourseRatings(@Path("courseCode") String courseCode);

    @POST(MarshalServiceProvider.POST_RATING + "{courseObjectId}")
    Call<Rating> postRating(@Path("courseObjectId") String courseObjectId, @Body Rating ratingObject);

    @PUT(MarshalServiceProvider.PUT_RATING + "{courseObjectId}")
    Call<Rating> updateRating(@Path("courseObjectId") String courseObjectId, @Body Rating ratingObject);

    @HTTP(method = "DELETE", path = MarshalServiceProvider.DELETE_RATING + "{courseObjectId}", hasBody = true)
    Call<Rating> deleteRating(@Path("courseObjectId") String courseObjectId, @Body Rating ratingObject);

    @GET(MarshalServiceProvider.GET_ALL_MALSHAB_ITEMS)
    Call<List<MalshabItem>> getAllMalshabItems();

    //******** FCM ********//
    @POST(MarshalServiceProvider.POST_FCM_REGISTER_NEW_DEVICE)
    Call<FcmRegistration> fcmRegisterNewDevice(@Body FcmRegistration fcmRegistration);

//    @PUT(MarshalServiceProvider.PUT_FCM_REGISTER_EXIST_DEVICE)
//    Call<FcmRegistration> fcmRegisterExistDevice(@Body FcmRegistration fcmRegistration);
//
//    @DELETE(MarshalServiceProvider.DELETE_FCM_UNREGISTER_DEVICE + "{hardwareId}")
//    Call<Rating> deleteFcmRegistration(@Path("hardwareId") String hardwareId);

    @POST(MarshalServiceProvider.POST_SUBSCRIBE_COURSE + "{hardwareId}" + "\\" + "{course_id}")
    Call<Void> subsribeCourse(@Path("hardwareId") String hardwareId, @Path("course_id") int courseID);

    @DELETE(MarshalServiceProvider.DELETE_UNSUBSCRIBE_COURSE + "{hardwareId}" + "\\" + "{course_id}")
    Call<Void> unsubsribeCourse(@Path("hardwareId") String hardwareId, @Path("course_id") int courseID);

//    @GET(MarshalServiceProvider.GET_FCM_REGISTRATION)
//    Call<FcmRegistration> getRegistration(@Path("hardwareId") String hardwareId);

    // ******** Settings ********//
    @GET(MarshalServiceProvider.GET_SETTINGS)
    Call<Settings> getSettings();

    // ******** Auth ***********//
    @POST(MarshalServiceProvider.AUTH)
    Call<String> auth(@Body AuthRequest authRequest);
}
