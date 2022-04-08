package com.company.art_and_culture.myarts.network

import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.pojo.ServerRequest
import com.company.art_and_culture.myarts.pojo.ServerResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface MuseumArtsService {

    @POST("indexMuseumGetArts.php")
    suspend fun getMuseumArts(@Body request: ServerRequest): Response<ServerResponse>

    companion object {
        const val INITIAL_PAGE_NUMBER = 1

        fun create(): MuseumArtsService {

            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MuseumArtsService::class.java)
        }
    }
}

interface MainHttpService {

    @POST("indexMyArts.php")
    suspend fun mainRequest(@Body request: ServerRequest): Response<ServerResponse>

    companion object {
        const val INITIAL_PAGE_NUMBER = 1

        fun create(): MainHttpService {

           return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MainHttpService::class.java)
        }
    }
}

