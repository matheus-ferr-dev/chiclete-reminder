package com.chiclete.reminder.app.api;

import com.chiclete.reminder.app.dto.CreateUserRequest;
import com.chiclete.reminder.app.dto.LoginRequest;
import com.chiclete.reminder.app.dto.LoginResponse;
import com.chiclete.reminder.app.dto.ReminderResponse;
import com.chiclete.reminder.app.dto.UserResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("users")
    Call<UserResponse> register(@Body CreateUserRequest body);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("reminders")
    Call<List<ReminderResponse>> reminders(@Header("Authorization") String auth);
}
