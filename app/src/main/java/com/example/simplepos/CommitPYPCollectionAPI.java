package com.example.simplepos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//DP - set all the query parameters
public interface CommitPYPCollectionAPI {

    @GET("R360.php")
    Call<String> STRING_CALL(
            @Query("Function") String Function,
            @Query("CereliaAPIKey") String CereliaAPIKey,
            @Query("CollectionID") String CollectionID,
            @Query("DeviceID") String DeviceID,
            @Query("VendorID") String VendorID,
            @Query("PolicyID") String PolicyID,
            @Query("ReceiptNumber") String ReceiptNumber,
            @Query("AmountInCents") double AmountInCents,
            @Query("MonthPaidFor") String MonthPaidFor,
            @Query("YearPaidFor") String YearPaidFor,
            @Query("MPIA") int MPIA,
            @Query("SettlementType") String SettlementType,
            @Query("RRN") String RRN,
            @Query("Connectivity") String Connectivity,
            @Query("ContactNumber") String ContactNumber,
            @Query("Date") String Date,
            @Query("IDType") String IDType,
            @Query("IDValue") String IDValue,
            @Query("RequestID") String RequestID,
            @Query("Reference") String Reference
    );
}