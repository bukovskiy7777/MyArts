package com.company.art_and_culture.myarts.network;

import com.company.art_and_culture.myarts.pojo.ServerRequest;
import com.company.art_and_culture.myarts.pojo.ServerResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkQuery {

    private String URL;
    private ServerRequest request;
    private static NetworkQuery mInstance;

    public static synchronized NetworkQuery getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkQuery();
        }
        return mInstance;
    }

    public Call<ServerResponse> create (String URL, ServerRequest request) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<ServerResponse> response = retrofitInterface.operation(request);
        return response;
    }

}
