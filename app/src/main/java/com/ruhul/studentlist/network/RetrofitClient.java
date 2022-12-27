package com.ruhul.studentlist.network;

import android.database.Observable;

import androidx.annotation.NonNull;

import com.ruhul.studentlist.model.SliderBannerResponse;
import com.ruhul.studentlist.signup.RegistrationResponse;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RetrofitClient {
    public static ApiServices apiServices;

    public static ApiServices getApiServices() {
        if (apiServices == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            client.addInterceptor(logging);
            client.addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .build();

                    Request.Builder requestBuilder = original.newBuilder().url(url);
                    requestBuilder.addHeader("Accept", "application/json");
                    requestBuilder.addHeader("Content-Type", "application/json");
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
            // Create retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("")
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

            apiServices = retrofit.create(ApiServices.class);
        }

        return apiServices;
    }

    public interface ApiServices {
        @GET("")
        Observable<SliderBannerResponse> getSliders();


        @FormUrlEncoded
        @POST("")
        Observable<RegistrationResponse> userSignup(
                @Field("name") String name,
                @Field("email") String email,
                @Field("mobile") String mobile_no,
                @Field("password") String password,
                @Field("password_confirmation") String passwordConfirmation
        );

    }
}
