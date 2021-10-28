package com.company.art_and_culture.myarts.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CallbackWithRetry<T> implements Callback<T> {

    private static final int TOTAL_RETRIES = 3;
    private static final String TAG = CallbackWithRetry.class.getSimpleName();
    private int retryCount = 0;

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (retryCount++ < TOTAL_RETRIES) {
            retry(call);
        }
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!response.isSuccessful()) {
            if (retryCount++ < TOTAL_RETRIES) {
                retry(call);
            }
        }
    }

    private void retry(Call<T> call) {
        call.clone().enqueue(this);
    }
}