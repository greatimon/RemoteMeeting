package com.example.jyn.remotemeeting.Util;

import com.example.jyn.remotemeeting.Etc.Static;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by JYN on 2017-12-01.
 */

/** 레트로핏 서비스제너레이터 - 레트로핏 중복 코드 줄이기 */
public class ServiceGenerator {

    private static final String BASE_URL = Static.SERVER_URL;

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

}