package com.example.simplepos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//DP - set all the query parameters
public interface ApiLoginPage {
    @GET("R360.php")
    Call<String> STRING_CALL(
            @Query("Function") String Function,
            @Query("VendorPin") String VendorPin,
            @Query("CereliaAPIKey") String CereliaAPIKey,
            @Query("DeviceID") String DeviceID, //macAddress
            @Query("RequestID") String RequestID
    );
}