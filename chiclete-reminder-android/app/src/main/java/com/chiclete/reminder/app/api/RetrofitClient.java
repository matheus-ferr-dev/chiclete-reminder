package com.chiclete.reminder.app.api;

import com.chiclete.reminder.app.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile ApiService api;

    private RetrofitClient() {
    }

    public static ApiService get() {
        if (api == null) {
            synchronized (RetrofitClient.class) {
                if (api == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BASIC);
                    OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(log)
                        .build();
                    Retrofit rf = new Retrofit.Builder()
                        .baseUrl(BuildConfig.API_BASE)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                    api = rf.create(ApiService.class);
                }
            }
        }
        return api;
    }
}
