package com.example.simplepos;

//DP - include all the libraries
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * $Authored_by: Damien Ellis
 * $Date: 2022/04/12.
 * $Description: Allows the user to login in and sets up the auth tokens
 */

public class LoginPage extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {

    //DP - Initialize variables
    private EditText etUserPin;
    public Button LoginBtn;
    public String BASE_URL = "https://web09.pol360.co.za/receipt/", HandheldAuthToken, HandheldLastLogin, UserLogin, ConnectID;
    private ProgressBar LoginPageIndicator;

    protected LocationManager locationManager;
    private String lat, GetDeviceID,  GetCereriaApiKey, DeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login_page);

        //DP - assign elements to variables
        LoginBtn = findViewById(R.id.LoginBtn);
        etUserPin = findViewById(R.id.etUserPin);
        LoginPageIndicator = findViewById(R.id.LoginPageIndicator);

        //DP - start the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //DP - login onclick listener
        LoginBtn.setOnClickListener(view -> {

            //DP - Ensure the User pin is not empty
            if (!(etUserPin.getText().toString().equals(""))) {

                //DP - set the indicator to visible
                LoginPageIndicator.setVisibility(View.VISIBLE);

                //DP - Call the login method and pass the user pin
                UserLogin (etUserPin.getText().toString());

            } else {

                //DP - Show warning message
                Toast.makeText(LoginPage.this, "Please enter User Pin", Toast.LENGTH_LONG).show();
            }
        });
    }

    //DP - helper method to log the user in.
    public void UserLogin (String UserPin) {

        //DP - obtain retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        //DP - Create ApiLoginPage interface
        ApiLoginPage ApiInterface = retrofit.create(ApiLoginPage.class);

        //DP - initialize call
        Call<String> call = ApiInterface.STRING_CALL("AuthenticateDeviceVendor", UserPin, "BRYTE1234", "123", "1234");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

                //DP - check condition if there is a valid response
                if (response.isSuccessful() && response.body() != null) {

                    try {

                        //DP - get the JSON data
                        JSONObject jsonObject = new JSONObject(response.body());

                        //DP - ensure we have the OK response
                        if (jsonObject.getString("Result").equals("OK")) {

                            //DP - Assign variables
                            HandheldAuthToken = jsonObject.getString("HandheldAuthToken");
                            HandheldLastLogin = jsonObject.getString("HandheldLastLogin");
                            UserLogin = jsonObject.getString("UserLogin");
                            ConnectID = jsonObject.getString("ConnectID");

                            //DP - start the new intent to the policy listing page.
                            Intent intent = new Intent(LoginPage.this, ReferenceAmount.class);
                            //Set as explicit intent
                            intent.setPackage("com.example.simplepos");
                            intent.putExtra("UserPin", UserPin);
                            intent.putExtra("ConnectID", ConnectID);
                            intent.putExtra("UserLogin", UserLogin);
                            intent.putExtra("VendorID", jsonObject.getString("VendorID"));
                            intent.putExtra("HandheldAuthToken", HandheldAuthToken);
                            intent.putExtra("HandheldLastLogin", HandheldLastLogin);
                            intent.putExtra("DeviceID", GetDeviceID);
                            intent.putExtra("GetCereriaApiKey", GetCereriaApiKey);
                            intent.putExtra("DeviceType", DeviceType);
                            startActivity(intent);
                            finish();

                            //DP - display message to user
                            Toast.makeText(getApplicationContext(), "Welcome back " + jsonObject.getString("VendorReceiptName"), Toast.LENGTH_SHORT).show();

                            //DP - hide indicator
                            LoginPageIndicator.setVisibility(View.INVISIBLE);
                        } else {
                            //DP - hide indicator
                            LoginPageIndicator.setVisibility(View.INVISIBLE);
                            //DP - display warning to user
                            Toast.makeText(getApplicationContext(), jsonObject.getString("VendorMessage"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //DP - hide indicator
                        LoginPageIndicator.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Data response Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //DP - hide indicator
                    LoginPageIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Service Error", Toast.LENGTH_SHORT).show();
                }

                //DP - hide indicator
                LoginPageIndicator.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                //DP - hide indicator
                LoginPageIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Unexpected Error Occured "+t, Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if (isConnectedViaWifi()) {

            Toast.makeText(this, "Exit disabled if wifi is connected", Toast.LENGTH_SHORT).show();

        } else {

            this.doubleBackToExitPressedOnce = true;

            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        }


        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}