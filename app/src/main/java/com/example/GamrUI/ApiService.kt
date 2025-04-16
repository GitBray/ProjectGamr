package com.example.GamrUI

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Query

//This file defines all HTTP requests the app makes to the backend
//Retrofit uses this interface to generate the actual network call code behind the scenes.
//Each function corresponds to a PHP file in the XAMPP server

interface ApiService {
    @GET("get_feed.php")
    suspend fun getUserFeed(
        @Query("user_id") userId: Int
    ): Response<List<User>>

    //
    @POST("submit_swipe.php")
    @FormUrlEncoded
    suspend fun submitSwipe(
        @Field("swiper_id") swiperId: Int,
        @Field("swipee_id") swipeeId: Int,
        @Field("direction") direction: String
    ): Response<Map<String, Any>>
}

