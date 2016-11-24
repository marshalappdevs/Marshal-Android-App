package com.basmapp.marshal.util;

import com.basmapp.marshal.interfaces.IMarshalService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarshalServiceProvider {

    private static final String API_BASE_URL = "http://marshalbeta.azurewebsites.net/api/";
    public static final String GET_ALL_COURSES = API_BASE_URL + "courses";
    public static final String GET_ALL_MATERIALS = API_BASE_URL + "materials";
    public static final String POST_RATING = API_BASE_URL + "courses/ratings/";
    public static final String PUT_RATING = API_BASE_URL + "courses/ratings/";
    public static final String DELETE_RATING = API_BASE_URL + "courses/ratings/";
    public static final String POST_FCM_REGISTER_NEW_DEVICE = API_BASE_URL + "fcm/register/";
    public static final String GET_SETTINGS = API_BASE_URL + "settings/";
    public static final String GET_ALL_MALSHAB_ITEMS = API_BASE_URL + "malshabitems/";
    public static final String GET_ALL_FAQ_ITEMS = API_BASE_URL + "faq/";
    public static final String POST_SUBSCRIBE_COURSE = API_BASE_URL + "fcm/subscription/course/";
    public static final String DELETE_UNSUBSCRIBE_COURSE = API_BASE_URL + "fcm/subscription/course/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDateSerializer())
                    .registerTypeAdapter(Date.class, new JsonDateDeserializer())
                    .excludeFieldsWithoutExposeAnnotation()
                    .setLenient()
                    .create()));

    public static IMarshalService getInstance(final String authToken) {
        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "JWT " + authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(IMarshalService.class);
    }

    public static class JsonDateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = json.getAsJsonPrimitive().getAsString();
            Date d = new Date(Long.valueOf(s.substring(6, s.length() - 2)));
            return d;
        }
    }

    public static class JsonDateSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive("/Date(" + src.getTime() + ")/");
        }
    }
}
