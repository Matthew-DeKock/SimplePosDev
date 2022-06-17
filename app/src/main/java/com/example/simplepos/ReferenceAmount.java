package com.example.simplepos;

//DP - include all the libraries

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * $Authored_by: Damien Ellis
 * $Date: 2022/04/12.
 * $Description: Allows the user enter the payment amount and the reference
 */

public class ReferenceAmount extends AppCompatActivity {

    //DP - initialize all variables
    Button SubmitBtn, CancelBtn;
    public String Month;
    public int Year;
    public String UserPin;
    public String HandheldAuthToken;
    public String HandheldLastLogin;
    public String UserLogin;
    public String ConnectID;
    public String UserNumber;
    public String VendorID;
    public String DeviceID;
    public String GetCereriaApiKey;
    public String DeviceType;
    public float AmountInCents;
    public String BASE_URL = "https://web09.pol360.co.za/receipt/";
    public EditText Reference, OwnAmount;
    private ProgressBar ReferenceIndicator;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference_amount);

        //DP - Get policy info
        Intent intent = getIntent();

        UserNumber = intent.getStringExtra("UserNumber");
        UserPin = intent.getStringExtra("UserPin");
        ConnectID = intent.getStringExtra("ConnectID");
        UserLogin = intent.getStringExtra("UserLogin");
        VendorID = intent.getStringExtra("VendorID");
        HandheldAuthToken = intent.getStringExtra("HandheldAuthToken");
        HandheldLastLogin = intent.getStringExtra("HandheldLastLogin");
        DeviceID = intent.getStringExtra("DeviceID");
        GetCereriaApiKey = intent.getStringExtra("GetCereriaApiKey");
        DeviceType = intent.getStringExtra("DeviceType");

        dumpIntent("PaymentMethod", intent);

        SubmitBtn = findViewById(R.id.SubmitBtn);
        CancelBtn = findViewById(R.id.CancelBtn);
        Reference = findViewById(R.id.Reference);
        OwnAmount = findViewById(R.id.OwnAmount);
        ReferenceIndicator = findViewById(R.id.ReferenceIndicator);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();

        Month = dateFormat.format(date);

        Year = Calendar.getInstance().get(Calendar.YEAR);

        SubmitBtn.setOnClickListener(view -> {

            //DP - ensure reference is not empty
            if (Reference.getText().toString().equals("")) {
                Toast.makeText(ReferenceAmount.this, "Please enter a reference!", Toast.LENGTH_SHORT).show();

            //DP- ensure amount is not empty
            } else if (OwnAmount.getText().toString().equals("")) {
                Toast.makeText(ReferenceAmount.this, "Please enter an amount!", Toast.LENGTH_SHORT).show();
            } else {

                //DP - get member Details
                EnterNumber ("0004285229086", "SAIDNumber");
            }
        });

        CancelBtn.setOnClickListener(view -> {
            //DP - start the new intent to the policy listing page.
            Intent intentSend = new Intent(ReferenceAmount.this, LoginPage.class);
            intentSend.setPackage("com.example.simplepos");
            startActivity(intentSend);
            finish();
        });
    }

    //DP - helper method to get the members details
    public void EnterNumber (String UserInput, String IDType) {

        ReferenceIndicator.setVisibility(View.VISIBLE);

        //DP - build the retrofit call
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        //DP - Create main interface
        EnterNumberApi ApiInterface = retrofit.create(EnterNumberApi.class);

        //DP - initialize call
        Call<String> call = ApiInterface.STRING_CALL("GetMemberInfoViaIDNumber", "BRYTE1234", IDType, UserInput, "121", "1212");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

                //DP - check condition
                if (response.isSuccessful() && response.body() != null) {

                    //DP - when response is successful and not empty and initialize json array
                    try {

                        //DP - convert response to JSON object
                        JSONObject jsonObject = new JSONObject(response.body());

                        if (jsonObject.getString("Result").equals("OK")) {

                            Intent SendIntent = new Intent(ReferenceAmount.this, PaymentMethod.class);
                            //Set as explicit intent
                            SendIntent.setPackage("com.example.simplepos");
                            SendIntent.putExtra("UserPin", UserPin);
                            SendIntent.putExtra("ConnectID", ConnectID);
                            SendIntent.putExtra("UserLogin", UserLogin);
                            SendIntent.putExtra("VendorID", VendorID);
                            SendIntent.putExtra("HandheldAuthToken", HandheldAuthToken);
                            SendIntent.putExtra("HandheldLastLogin", HandheldLastLogin);
                            SendIntent.putExtra("DeviceID", DeviceID);
                            SendIntent.putExtra("GetCereriaApiKey", GetCereriaApiKey);
                            SendIntent.putExtra("DeviceType", DeviceType);
                            SendIntent.putExtra("Reference", Reference.getText().toString());
                            AmountInCents = Float.parseFloat(OwnAmount.getText().toString());
                            SendIntent.putExtra("AmountInCents",AmountInCents);
                            SendIntent.putExtra("Month", Month);
                            SendIntent.putExtra("Year", String.valueOf(Year));
                            SendIntent.putExtra("MPIA", 1);
                            startActivity(SendIntent);
                            finish();

                            ReferenceIndicator.setVisibility(View.INVISIBLE);
                        } else {

                            ReferenceIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("SystemMessage"), Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        ReferenceIndicator.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Data Response Error: Reference Amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Service Error", Toast.LENGTH_SHORT).show();
                    ReferenceIndicator.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                ReferenceIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
    }

    public static void dumpIntent(String Source, Intent i){
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(TAG,"Dumping Intent start: " + Source);
            while (it.hasNext()) {
                String key = it.next();
                Log.e(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(TAG,"Dumping Intent end");
        }
    }
}