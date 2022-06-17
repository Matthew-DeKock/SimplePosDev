package com.example.simplepos;

//DP - include all the libraries
import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.newland.sdk.ModuleManage;
import com.newland.sdk.module.printer.ErrorCode;
import com.newland.sdk.module.printer.PrintListener;
import com.newland.sdk.module.printer.PrintScriptUtil;
import com.newland.sdk.module.printer.PrinterModule;
import com.newland.sdk.module.printer.PrinterStatus;
import com.newland.sdk.module.printer.PrinterStatusListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * $Authored_by: Damien Ellis
 * $Date: 2022/04/12.
 * $Description: Allows the user to make a payment
 */

public class PaymentMethod extends AppCompatActivity {

    //DP - initialize the variables
    Button CashPayment, CreditPayment, BackBtn, HomeBtn;
    public String GetIDNumber;
    public String GetSupPolPaymentTypeID;
    public String ClientNameString;
    public String PolicyBalance;
    public String PolicyName;
    public String PolicyPremium;
    public String PolicyHistID;
    public String Month;
    public String CellNumber;
    public String ReceiptNumber;
    public String ReceiptDate;
    public String MonthsPaidFor;
    public String SettlementType;
    public String UserPin;
    public String HandheldAuthToken;
    public String HandheldLastLogin;
    public String UserLogin;
    public String ConnectID;
    public String UserNumber;
    public String Action;
    public String GetConnectID;
    public String GlobalAction;
    public String PayType;
    public String VendorID;
    public String RRN;
    public String DeviceID;
    public String GetCereriaApiKey;
    public String DeviceType;
    public String Reference;

    public int MPIA, Year;
    public double AmountInCents, OrigAmount;
    public String BASE_URL = "https://web09.pol360.co.za/receipt/";
    private static final int REQUEST_CODE = 1;

    private PrinterModule printerModule;
    private  ModuleManage moduleManage;

    private Boolean Committransaction = false, CardPayment = false;

    private ProgressBar SelectPaymentIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        //DP - setup the module manage instance
        moduleManage = ModuleManage.getInstance();

        //DP - check the connection status
        boolean connectStatus= moduleManage.init(this);
        if(!connectStatus){
            Toast.makeText(this, "Device not connected properly!", Toast.LENGTH_SHORT).show();
        }

        //DP - initialize the printer module
        printerModule = moduleManage.getPrinterModule();

        //DP - Get policy info
        Intent intent = getIntent();

        //DP - assign the variables their values
        String UserNumber = intent.getStringExtra("UserNumber");
        GetConnectID = intent.getStringExtra("ConnectID");
        AmountInCents  =  intent.getFloatExtra("AmountInCents", 0);
        OrigAmount = AmountInCents;
        AmountInCents = AmountInCents * 100;

        VendorID = intent.getStringExtra("VendorID");
        Reference = intent.getStringExtra("Reference");

        Month = intent.getStringExtra("Month");
        Year = intent.getIntExtra("Year", 0);
        MPIA = intent.getIntExtra("MPIA", 0);

        Action = intent.getStringExtra("Action");
        UserPin = intent.getStringExtra("UserPin");
        UserLogin = intent.getStringExtra("UserLogin");
        ConnectID = intent.getStringExtra("ConnectID");
        HandheldAuthToken = intent.getStringExtra("HandheldAuthToken");
        HandheldLastLogin = intent.getStringExtra("HandheldLastLogin");

        DeviceType = "POS";

        dumpIntent("PaymentMethod", intent);

        CashPayment = findViewById(R.id.CashPayment);
        CreditPayment = findViewById(R.id.CreditPayment);
        HomeBtn = findViewById(R.id.HomeBtn);

        if (DeviceType.equals("Mobile")) {
            CreditPayment.setVisibility(View.GONE);
        } else {
            CreditPayment.setVisibility(View.VISIBLE);
        }

        BackBtn = findViewById(R.id.BackBtn);
        SelectPaymentIndicator = findViewById(R.id.SelectPaymentIndicator);

        //DP - set the onclick listener for the cash payment
        CashPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //DP - check the status of the printer
                boolean connectStatus= moduleManage.init(getApplicationContext());
                if(!connectStatus){
                    Toast.makeText(getApplicationContext(), "Device not connected properly!", Toast.LENGTH_SHORT).show();

                } else {

                    PayType = "Cash";

                    //DP - prevalidate the member
                    PrevalidatePYPCollection();
                }
            }
        });

        //DP - set onclick listener for the Bank card payment method
        CreditPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PayType = "BankCard";
                CardPayment = true;

                //DP - prevalidate the member
                PrevalidatePYPCollection();
            }
        });

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DP - Get policy info
                Intent intent = getIntent();
                String PolicyNumber = intent.getStringExtra("PolicyNumber");
                String PolicyPlan = intent.getStringExtra("PolicyPlan");
                Integer MonthlyPayment = intent.getIntExtra("MonthlyPayment", 0);
                Integer Balance = intent.getIntExtra("Balance", 0);
                double ExpectedPay = MonthlyPayment * 1;
                //DP - start the new intent to the policy listing page.
                Intent intentSend = new Intent(PaymentMethod.this, ReferenceAmount.class);
                intentSend.setPackage("com.example.simplepos");
                intentSend.putExtra("PolicyNumber", PolicyNumber);
                intentSend.putExtra("PolicyPlan", PolicyPlan);
                intentSend.putExtra("MonthlyPayment", MonthlyPayment);
                intentSend.putExtra("Balance", Balance);
                intentSend.putExtra("ExpectedPay", ExpectedPay);
                intentSend.putExtra("CellNumber", CellNumber);
                intentSend.putExtra("IDNumber", GetIDNumber);
                intentSend.putExtra("ConnectID", GetConnectID);
                intentSend.putExtra("ClientName", ClientNameString);
                intentSend.putExtra("PolicyName", PolicyName);
                intentSend.putExtra("Premium", PolicyPremium);
                intentSend.putExtra("PolicyBalance", PolicyBalance);
                intentSend.putExtra("SupPolPaymentTypeID", GetSupPolPaymentTypeID);
                intentSend.putExtra("PolicyHistID", PolicyHistID);
                //DP - set month and year for the next pages
                intentSend.putExtra("Month", Month);
                intentSend.putExtra("Year", Year);
                intentSend.putExtra("AmountInCents", AmountInCents);
                intentSend.putExtra("Action",Action);
                intentSend.putExtra("UserPin", UserPin);
                intentSend.putExtra("UserLogin", UserLogin);
                intentSend.putExtra("HandheldAuthToken", HandheldAuthToken);
                intentSend.putExtra("HandheldLastLogin", HandheldLastLogin);
                intentSend.putExtra("UserNumber", UserNumber);
                intentSend.putExtra("GlobalAction", GlobalAction);
                intentSend.putExtra("VendorID", VendorID);
                intentSend.putExtra("DeviceID", DeviceID);
                intentSend.putExtra("GetCereriaApiKey", GetCereriaApiKey);
                intentSend.putExtra("DeviceType", DeviceType);
                startActivity(intentSend);
                finish();
            }
        });

        HomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentSend = new Intent(PaymentMethod.this, LoginPage.class);
                intentSend.setPackage("com.example.simplepos");
                startActivity(intentSend);
                finish();
            }
        });
    }

    //Method prevalidates the member for the transaction
    public void PrevalidatePYPCollection() {
        CashPayment.setEnabled(false);
        CreditPayment.setEnabled(false);

        //DP - show indicator
        SelectPaymentIndicator.setVisibility(View.VISIBLE);

        //DP - initialize the retrofit call
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        //DP - Create main interface
        PrevalidatePYPCollectionApi ApiInterface = retrofit.create(PrevalidatePYPCollectionApi.class);

        //DP - initialize call
        Call<String> call = ApiInterface.STRING_CALL(
                "PrevalidatePYPCollection", "BRYTE1234", "",
                "123", VendorID, "1",
                AmountInCents, Month, String.valueOf(Year),
                MPIA, "123", PayType,
                "", "0607876384", "123", "123", "100"
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {

                //DP - check condition
                if (response.isSuccessful() && response.body() != null) {

                    //DP - when response is successful and not empty and intilize json array
                    try {

                        //DP - Convert response body to JSON object
                        JSONObject jsonObject = new JSONObject(response.body());

                        //DP - check if response is okay
                        if (jsonObject.getString("Result").equals("OK")) {

                            if (CardPayment.equals(true)) {

                                //DP - do the purchase and wait for its response
                                doPurchase();

                            } else {
                                Committransaction = true;

                                if (Committransaction.equals(true)) {
                                    CommitPYPCollection();
                                }
                            }
                        } else {
                            CashPayment.setEnabled(true);
                            CreditPayment.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error validating transaction ! " + jsonObject.getString("VendorMessage"), Toast.LENGTH_SHORT).show();
                            Committransaction = false;
                        }

                    } catch (JSONException e) {
                        CashPayment.setEnabled(true);
                        CreditPayment.setEnabled(true);
                        //e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Data Response Error: Payment Method", Toast.LENGTH_SHORT).show();
                        SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                    }
                } else {
                    CashPayment.setEnabled(true);
                    CreditPayment.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Service Error", Toast.LENGTH_SHORT).show();
                    SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                CashPayment.setEnabled(true);
                CreditPayment.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Server Error: Payment Method", Toast.LENGTH_SHORT).show();
                SelectPaymentIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void CommitPYPCollection() {

        CashPayment.setEnabled(false);
        CreditPayment.setEnabled(false);

        //DP - build the retrofit call
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        //DP - Create main interface
        CommitPYPCollectionAPI ApiInterface = retrofit.create(CommitPYPCollectionAPI.class);

        //DP - initialize call
        Call<String> call = ApiInterface.STRING_CALL(
                "CommitPYPCollection", "BRYTE1234", "BRYTE1234",
                "1234", VendorID, "1",
                ReceiptNumber, AmountInCents, Month,
                String.valueOf(Year), MPIA,  PayType,
                RRN, "Online", "0607876384",
                "1234", "SAIDNumber", "0004285229086", "1234", Reference
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {

                //DP - check condition
                if (response.isSuccessful() && response.body() != null) {

                    //DP - when response is successful and not empty and initialize json array
                    try {

                        //DP - convert response body to JSON object
                        JSONObject jsonObject = new JSONObject(response.body());

                        //DP - check if response is okay
                        if (jsonObject.getString("Result").equals("OK")) {

                            Toast.makeText(PaymentMethod.this, "Creating Transaction record", Toast.LENGTH_SHORT).show();

                            //DP - assign variables
                            ReceiptNumber = jsonObject.getString("ReceiptNumber");
                            ReceiptDate = jsonObject.getString("ReceiptDate");
                            MonthsPaidFor = jsonObject.getString("MonthPaidFor");
                            SettlementType = jsonObject.getString("SettleMentType");

                            //invoke the receipt print
                            if (DeviceType.equals("POS")) {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        printByScript();
                                    }
                                }, 2000);
                            }

                            if (CardPayment.equals(true)) {

                            } else {

                                CashPayment.setEnabled(true);
                                CreditPayment.setEnabled(true);

                                //DP - set the dialog
                                SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                                Dialog dialog = new Dialog(PaymentMethod.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.setContentView(R.layout.redirect_popup);

                                Button ContinueBtn = dialog.findViewById(R.id.ContinueButton);

                                dialog.show();
                                ContinueBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        //DP - Get policy info
                                        Intent intent = getIntent();
                                        String PolicyNumber = intent.getStringExtra("PolicyNumber");
                                        String PolicyPlan = intent.getStringExtra("PolicyPlan");
                                        Integer MonthlyPayment = intent.getIntExtra("MonthlyPayment", 0);
                                        Integer Balance = intent.getIntExtra("Balance", 0);
                                        double ExpectedPay = MonthlyPayment * 1;
                                        //DP - start the new intent to the policy listing page.
                                        Intent intentSend = new Intent(PaymentMethod.this, ReferenceAmount.class);
                                        intentSend.setPackage("com.example.simplepos");
                                        intentSend.putExtra("PolicyNumber", PolicyNumber);
                                        intentSend.putExtra("PolicyPlan", PolicyPlan);
                                        intentSend.putExtra("MonthlyPayment", MonthlyPayment);
                                        intentSend.putExtra("Balance", Balance);
                                        intentSend.putExtra("ExpectedPay", ExpectedPay);
                                        intentSend.putExtra("CellNumber", CellNumber);
                                        intentSend.putExtra("IDNumber", GetIDNumber);
                                        intentSend.putExtra("ConnectID", GetConnectID);
                                        intentSend.putExtra("ClientName", ClientNameString);
                                        intentSend.putExtra("PolicyName", PolicyName);
                                        intentSend.putExtra("Premium", PolicyPremium);
                                        intentSend.putExtra("PolicyBalance", PolicyBalance);
                                        intentSend.putExtra("SupPolPaymentTypeID", GetSupPolPaymentTypeID);
                                        intentSend.putExtra("PolicyHistID", PolicyHistID);
                                        //DP - set month and year for the next pages
                                        intentSend.putExtra("Month", Month);
                                        intentSend.putExtra("Year", Year);
                                        intentSend.putExtra("AmountInCents", AmountInCents);
                                        intentSend.putExtra("Action",Action);
                                        intentSend.putExtra("UserPin", UserPin);
                                        intentSend.putExtra("UserLogin", UserLogin);
                                        intentSend.putExtra("HandheldAuthToken", HandheldAuthToken);
                                        intentSend.putExtra("HandheldLastLogin", HandheldLastLogin);
                                        intentSend.putExtra("UserNumber", UserNumber);
                                        intentSend.putExtra("GlobalAction", GlobalAction);
                                        intentSend.putExtra("VendorID", VendorID);
                                        intentSend.putExtra("DeviceID", DeviceID);
                                        intentSend.putExtra("GetCereriaApiKey", GetCereriaApiKey);
                                        intentSend.putExtra("DeviceType", DeviceType);
                                        startActivity(intentSend);
                                        finish();
                                    }
                                });
                            }
                        } else {
                            CashPayment.setEnabled(true);
                            CreditPayment.setEnabled(true);
                            SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Enexpected error occured !" + jsonObject.getString("VendorMessage"), Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        CashPayment.setEnabled(true);
                        CreditPayment.setEnabled(true);
                        //e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Data Response Error", Toast.LENGTH_SHORT).show();
                        SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                    }
                } else {
                    CashPayment.setEnabled(true);
                    CreditPayment.setEnabled(true);
                    SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Service Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                CashPayment.setEnabled(true);
                CreditPayment.setEnabled(true);
                SelectPaymentIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getState() {
        try {
            Toast.makeText(this, "Printer Status: " + printerModule.getStatus() + "\n", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Printer Status exception: " + e + "\n", Toast.LENGTH_SHORT).show();
        }
    }

    //DP - helper method to print the script
    private void printByScript() {
        if (printerModule.getStatus() != PrinterStatus.NORMAL) {
            Toast.makeText(this, "Print Failed! Printer status is not normal" + "\r\n", Toast.LENGTH_SHORT).show();
        } else {
            try {
                AmountInCents = AmountInCents / 100;
                String AmountInCents2 = String.format("%.2f", AmountInCents);
                // ------------------------------------------------------------
                // Note: For details about script rules, see <TTF_Script_print_command_standard.pdf> in the doc directory of the compressed documentation package.  ！！！
                // ------------------------------------------------------------
                StringBuffer scriptBuffer = new StringBuffer();
                String fontsPath = printerModule.setFont(getApplicationContext(), "simsun.ttc");
                if (fontsPath != null) {
                    scriptBuffer.append("!font " + fontsPath + "\n");//set font
                }

                scriptBuffer.append("*line\n!gray 3\n!yspace 10\n");
                scriptBuffer.append("!hz l\n!asc l\n*text c "+ "Bryte" +"\n");//Both Chinese and English are set as L font and printed to the right
                scriptBuffer.append("*line\n!gray 3\n!yspace 10\n");
                scriptBuffer.append("!hz n\n!asc n\n*text c " + "Receipt" + "\n");
                scriptBuffer.append("*line\n!gray 3\n!yspace 10\n");
                scriptBuffer.append("!NLFONT 25 35 3\n*TEXT l Receipt Number:"+"\n!NLFONT 25 35 3\n*text r "+ReceiptNumber+"\n");
                scriptBuffer.append("!NLFONT 25 35 3\n*TEXT l Reference:"+"\n!NLFONT 25 35 3\n*text r " + Reference +"\n");
                scriptBuffer.append("!NLFONT 25 35 3\n*TEXT l Amount Paid:"+"\n!NLFONT 25 35 3\n*text r R"+ AmountInCents2 +"\n");
                scriptBuffer.append("!NLFONT 25 35 3\n*TEXT l Date:"+"\n!NLFONT 25 35 3\n*text r " + ReceiptDate +"\n");
                scriptBuffer.append("!hz s\n!asc n\n*text l "+"Month Paid For:"+"\n"); //Both Chinese and English are set as S font and printed to the left
                scriptBuffer.append("!hz s\n!asc n\n*text l "+MonthsPaidFor+"\n"); //Both Chinese and English are set as S font and printed to the left
                scriptBuffer.append("*line\n!gray 3\n!yspace 20\n");
                scriptBuffer.append("*feedline 3\n");

                Map<String, Bitmap> map = new HashMap<String, Bitmap>();

                printerModule.print(scriptBuffer.toString(), map, new PrintListener() {
                    @Override
                    public void onSuccess() {
                        //Log.d(TAG, "Print Successfully: " );
                    }

                    @Override
                    public void onError(ErrorCode error, String msg) {
                        //Toast.makeText(PaymentMethod.this, "Print failed: " + error + "   error msg " + msg + "\n", Toast.LENGTH_SHORT).show();
                    }
                });

                getState();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PaymentMethod.this, "Print failed: " + e, Toast.LENGTH_SHORT).show();
            }
        }
        getState();
    }

    private void setStatusListener() {
        try {
            printerModule.setStatusListener(new PrinterStatusListener() {
                @Override
                public void onStatus(PrinterStatus status) {
                    Toast.makeText(PaymentMethod.this, "PrinterStatus:"+status, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PaymentMethod.this, e.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    //DP - helper method to perform the card payment
    public boolean doPurchase() {
        boolean foundIntent = false;

        //Link to dash pay app
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) { //com.dashpay.bridge
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains("com.ar.dashpaypos")
                        || info.activityInfo.name.toLowerCase().contains("com.ar.dashpaypos")) {

                    final Currency curr = Currency.getInstance("ZAR");
                    final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
                    nf.setCurrency(curr);
                    String value = nf.format(OrigAmount);

                    value = value.replace(',', '.');
                    value = value.replace('R', ' ');
                    value = value.trim();
                    value = value.replaceAll("\\s+","");
                    Log.d(TAG, "Money: " + value);
                    share.putExtra(Intent.EXTRA_ORIGINATING_URI,"com.example.simplepos");
                    share.putExtra("TRANSACTION_TYPE","PURCHASE");
                    share.putExtra("AMOUNT",value);
                    share.putExtra("TRANSACTION_ID","123e4567-e89b-12d3-a456-426614174111");
                    share.putExtra("ALLOW_FALLBACK","No");

                    share.setPackage(info.activityInfo.packageName);
                    foundIntent = true;
                    break;
                }
            }
            if (!foundIntent)
                return false;

            startActivityForResult(Intent.createChooser(share,"SimplePOS"),REQUEST_CODE);
            return true;
        }
        return false;
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //DP - get response from dashpay
        if (requestCode == REQUEST_CODE) {

            SelectPaymentIndicator.setVisibility(View.INVISIBLE);

            String displayTest = intent.getStringExtra("DISPLAY_TEXT");

            dumpIntent("CardPayment Returned", intent);

            //DP - set the popup dialog
            Dialog dialog = new Dialog(PaymentMethod.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.redirect_popup);

            Button ContinueBtn = dialog.findViewById(R.id.ContinueButton);
            TextView Msg = dialog.findViewById(R.id.Header);

            RRN = intent.getStringExtra("RRN");

            if (displayTest.equals("APPROVED")) {
                ContinueBtn.setText("Continue");

                //DP - commit the transaction
                CommitPYPCollection();

                Msg.setText("Transaction: "+ displayTest);

                //DP - show the dialog
                dialog.show();

                CashPayment.setEnabled(true);
                CreditPayment.setEnabled(true);

                ContinueBtn.setOnClickListener(view -> {
                    dialog.dismiss();
                    //DP - Get policy info
                    Intent intent1 = getIntent();
                    String PolicyNumber = intent1.getStringExtra("PolicyNumber");
                    String PolicyPlan = intent1.getStringExtra("PolicyPlan");
                    Integer MonthlyPayment;
                    MonthlyPayment = intent1.getIntExtra("MonthlyPayment", 0);
                    Integer Balance = intent1.getIntExtra("Balance", 0);
                    double ExpectedPay;
                    ExpectedPay = MonthlyPayment;
                    //DP - start the new intent to the policy listing page.
                    Intent intentSend = new Intent(PaymentMethod.this, ReferenceAmount.class);
                    intentSend.setPackage("com.example.simplepos");
                    intentSend.putExtra("PolicyNumber", PolicyNumber);
                    intentSend.putExtra("PolicyPlan", PolicyPlan);
                    intentSend.putExtra("MonthlyPayment", MonthlyPayment);
                    intentSend.putExtra("Balance", Balance);
                    intentSend.putExtra("ExpectedPay", ExpectedPay);
                    intentSend.putExtra("CellNumber", CellNumber);
                    intentSend.putExtra("IDNumber", GetIDNumber);
                    intentSend.putExtra("ConnectID", GetConnectID);
                    intentSend.putExtra("ClientName", ClientNameString);
                    intentSend.putExtra("PolicyName", PolicyName);
                    intentSend.putExtra("Premium", PolicyPremium);
                    intentSend.putExtra("PolicyBalance", PolicyBalance);
                    intentSend.putExtra("SupPolPaymentTypeID", GetSupPolPaymentTypeID);
                    intentSend.putExtra("PolicyHistID", PolicyHistID);
                    //DP - set month and year for the next pages
                    intentSend.putExtra("Month", Month);
                    intentSend.putExtra("Year", Year);
                    intentSend.putExtra("AmountInCents", AmountInCents);
                    intentSend.putExtra("Action",Action);
                    intentSend.putExtra("UserPin", UserPin);
                    intentSend.putExtra("UserLogin", UserLogin);
                    intentSend.putExtra("HandheldAuthToken", HandheldAuthToken);
                    intentSend.putExtra("HandheldLastLogin", HandheldLastLogin);
                    intentSend.putExtra("UserNumber", UserNumber);
                    intentSend.putExtra("GlobalAction", GlobalAction);
                    intentSend.putExtra("VendorID", VendorID);
                    intentSend.putExtra("DeviceID", DeviceID);
                    intentSend.putExtra("GetCereriaApiKey", GetCereriaApiKey);
                    intentSend.putExtra("DeviceType", DeviceType);
                    startActivity(intentSend);
                    finish();
                });
            } else {
                CashPayment.setEnabled(true);
                CreditPayment.setEnabled(true);
                Msg.setText("Transaction: "+ displayTest);
                ContinueBtn.setText("OK");
                dialog.show();
                ContinueBtn.setOnClickListener(view -> dialog.dismiss());
            }

            //DP - set card payment to false for re-initializing
            CardPayment = false;
        }
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