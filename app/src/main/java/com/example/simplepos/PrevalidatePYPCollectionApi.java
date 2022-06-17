package com.example.simplepos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//DP - set all the query parameters
public interface PrevalidatePYPCollectionApi {

    @GET("R360.php")
    Call<String> STRING_CALL(
            @Query("Function") String Function,
            @Query("CereliaAPIKey") String CereliaAPIKey,
            @Query("CollectionID") String CollectionID,
            @Query("DeviceID") String DeviceID,
            @Query("VendorID") String VendorID,
            @Query("PolicyID") String PolicyID,
            @Query("AmountInCents") double AmountInCents,
            @Query("MonthPaidFor") String MonthPaidFor,
            @Query("YearPaidFor") String YearPaidFor,
            @Query("MPIA") int MPIA,
            @Query("RequestID") String RequestID,
            @Query("SettlementType") String SettlementType,
            @Query("ReceiptNumber") String ReceiptNumber,
            @Query("ContactNumber") String ContactNumber,
            @Query("SigningID") String SigningID,
            @Query("ReqDate") String ReqDate,
            @Query("MaxDebitAmount") String MaxDebitAmount
    );
}
