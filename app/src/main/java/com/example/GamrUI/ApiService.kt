package com.example.GamrUI

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Query
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part



//This file defines all HTTP requests the app makes to the backend
//Retrofit uses this interface to generate the actual network call code behind the scenes.
//Each function corresponds to a PHP file in the XAMPP server

interface ApiService {
    @GET("get_feed.php")
    suspend fun getUserFeed(
        @Query("user_id") userId: Int
    ): Response<List<User>>

    @GET("get_matches.php")
    fun getMatches(
        @Query("user_id") userId: Int
    ): Call<List<User>>

    //
    @POST("submit_swipe.php")
    @FormUrlEncoded
    suspend fun submitSwipe(
        @Field("swiper_id") swiperId: Int,
        @Field("swipee_id") swipeeId: Int,
        @Field("direction") direction: String
    ): Response<Map<String, Any>>

    @Multipart
    @POST("update_file.php")
    fun updateProfileWithImage(
        @Part("user_id") userId: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part("discord") discord: RequestBody,
        @Part("instagram") instagram: RequestBody,
        @Part("playing_style") playingStyle: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("send_message.php")
    fun sendMessage(
        @Field("sender_id") senderId: Int,
        @Field("reciever_id") recieverId: Int,
        @Field("message") message: String,
        @Field("timestamp") timestamp: String
    ): Call<GenericResponse>

    @GET("get_messages.php")
    fun getMessages(
        @Query("user1") user1: Int,
        @Query("user2") user2: Int
    ): Call<List<Message>>

    @FormUrlEncoded
    @POST("update_file.php")
    fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("bio") bio: String,
        @Field("discord") discord: String,
        @Field("instagram") instagram: String,
        @Field("preferred_playstyle") playStyle: String
    ): Call<GenericResponse>

    @GET("get_profile.php")
    fun getProfile(
        @Query("user_id") userId: Int
    ): Call<UserProfile>

    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<GenericResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<GenericResponse>


}

