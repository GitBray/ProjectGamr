package com.example.GamrUI

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Base URL of the backend server
    // points to device where the folders where the PHP files are
    // replace ip address with your current ip address to work
    private const val BASE_URL = "http://138.47.151.162/gamr_api/"

    // interface used for API calls
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}