package com.basmach.marshal.interfaces;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.utils.MarshalServiceProvider;

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
}
