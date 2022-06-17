package com.example.simplepos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//DP - set all the query parameters
public interface EnterNumberApi {

    @GET("R360.php")
    Call<String> STRING_CALL(
            @Query("Function") String Function,
            @Query("CereliaAPIKey") String CereliaAPIKey,
            @Query("IDType") String IDType,
            @Query("ID") String ID,
            @Query("DeviceID") String DeviceID,
            @Query("RequestID") String RequestID
    );
}