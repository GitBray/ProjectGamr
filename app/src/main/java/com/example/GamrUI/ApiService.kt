package com.example.GamrUI

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Query

interface ApiService {
    @GET("get_feed.php")
    suspend fun getUserFeed(
        @Query("user_id") userId: Int
    ): Response<List<User>>

    @POST("submit_swipe.php")
    @FormUrlEncoded
    suspend fun submitSwipe(
        @Field("swiper_id") swiperId: Int,
        @Field("swipee_id") swipeeId: Int,
        @Field("direction") direction: String
    ): Response<Map<String, Any>>
}

