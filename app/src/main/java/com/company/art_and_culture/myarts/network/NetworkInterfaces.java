package com.company.art_and_culture.myarts.network;

import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

interface RetrofitInterface {
    @POST("indexMyArts.php")
    Call<ServerResponse> operation(@Body ServerRequest request);
}

interface MuseumFragmentGetArtsInterface {
    @POST("indexMuseumGetArts.php")
    Call<ServerResponse> operation(@Body ServerRequest request);
}

interface HomeFragmentGetArtsInterface {
    @POST("indexHomeGetArts.php")
    Call<ServerResponse> operation(@Body ServerRequest request);
}
