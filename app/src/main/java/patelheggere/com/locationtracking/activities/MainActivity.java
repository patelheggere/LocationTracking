package patelheggere.com.locationtracking.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import patelheggere.com.locationtracking.R;
import patelheggere.com.locationtracking.helper.SharedPreferenceHelper;
import patelheggere.com.locationtracking.receivers.LocationReceiver;
import patelheggere.com.locationtracking.services.LocationService;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static patelheggere.com.locationtracking.helper.utility.EMAIL;
import static patelheggere.com.locationtracking.helper.utility.FULL_NAME;
import static patelheggere.com.locationtracking.helper.utility.IMEI;
import static patelheggere.com.locationtracking.helper.utility.LOGIN_URL;
import static patelheggere.com.locationtracking.helper.utility.LOGOUT_URL;
import static patelheggere.com.locationtracking.helper.utility.TIME_INTERVAL;
import static patelheggere.com.locationtracking.helper.utility.isFirstTime;

public class MainActivity extends AppCompatActivity {

    private Button mButtonLogout, mButtonChangeTime, mButtonSave;
    private TextView mUserName;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private EditText mTimeEt;
    public static final int RequestPermissionCode = 7;

    private TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initilaizeVeiw();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(CheckingPermissionIsEnabledOrNot())
            {
                getDeviceImei();
                // Retrieve a PendingIntent that will perform a broadcast
                Intent alarmIntent = new Intent(this, LocationReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                sendLocationDetails();
                }

            // If, If permission is not enabled then else condition will execute.
            else {
                //Calling method to enable permission.
                RequestMultiplePermission();

            }
        }
        else
        {
            getDeviceImei();
            // Retrieve a PendingIntent that will perform a broadcast
            Intent alarmIntent = new Intent(this, LocationReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            sendLocationDetails();
        }

    }

    private void sendLocationDetails() {
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval =  SharedPreferenceHelper.getInstance().get(TIME_INTERVAL, 1)*60*1000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopLocationDetails() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            Toast.makeText(this, "Service Canceled", Toast.LENGTH_SHORT).show();
        }
    }


    private void getDeviceImei() {

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        String deviceid = mTelephonyManager.getDeviceId();
       SharedPreferenceHelper.getInstance().save(IMEI,deviceid);


    }

    private void initilaizeVeiw() {
        mButtonChangeTime = findViewById(R.id.changeConfig);
        mButtonSave = findViewById(R.id.buttonSave);
        mButtonLogout = findViewById(R.id.logoutbtn);
        mUserName = findViewById(R.id.tvusername);
        mTimeEt = findViewById(R.id.etTime);
        String name = SharedPreferenceHelper.getInstance().get(FULL_NAME, null);
        if(name!=null) {
            mUserName.setText(name);
            mUserName.setVisibility(View.VISIBLE);
        }
        else
        {
            mUserName.setVisibility(View.INVISIBLE);
        }
        //startService(new Intent(MainActivity.this, LocationService.class));
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceHelper.getInstance().save(isFirstTime, false);
               // stopService(new Intent(MainActivity.this, LocationService.class));
                logout();
                stopLocationDetails();
            }
        });

        mButtonChangeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeEt.setVisibility(View.VISIBLE);
                mButtonSave.setVisibility(View.VISIBLE);
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTimeEt.getText().toString()!=null) {
                    SharedPreferenceHelper.getInstance().save(TIME_INTERVAL, Integer.parseInt(mTimeEt.getText().toString()));
                    mTimeEt.setVisibility(View.INVISIBLE);
                    mButtonSave.setVisibility(View.INVISIBLE);
                    stopLocationDetails();
                    sendLocationDetails();
                }
                else
                    Toast.makeText(getApplicationContext(),"Please enter Time in minutes", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        String email = SharedPreferenceHelper.getInstance().get(EMAIL,null);
        JsonObjectRequest jsonObjectRequest = null;
        try {
            final JSONObject jsonBody = new JSONObject("{\"email\":"+email+" }");
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LOGOUT_URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(MainActivity.this, "Logged Out successfully", Toast.LENGTH_LONG).show();
                    stopLocationDetails();
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   Toast.makeText(MainActivity.this, "Some thing went wrong", Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }

    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        READ_PHONE_STATE
                }, RequestPermissionCode);

    }

    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean fine = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean coarse = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean phonestate = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    //boolean GetAccountsPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (fine && coarse && phonestate ) {

                        getDeviceImei();
                        // Retrieve a PendingIntent that will perform a broadcast
                        Intent alarmIntent = new Intent(this, LocationReceiver.class);
                        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                        sendLocationDetails();
                        //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED ;
    }

}
