package patelheggere.com.locationtracking.receivers;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.util.Date;

import patelheggere.com.locationtracking.R;
import patelheggere.com.locationtracking.helper.SharedPreferenceHelper;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.SystemClock.sleep;
import static patelheggere.com.locationtracking.helper.utility.BASE_URL;
import static patelheggere.com.locationtracking.helper.utility.IMEI;
import static patelheggere.com.locationtracking.helper.utility.IP_ADDRESS;
import static patelheggere.com.locationtracking.helper.utility.PORT;
import static patelheggere.com.locationtracking.helper.utility.UID;

public class LocationReceiver extends BroadcastReceiver {

    private static String TAG = "LocationReceiver";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10 * 60 * 1000;
    String imei;
    private static double Lat, Lon;
    private String mTimeStamp;
    private String outMsg;
    private Context mContext;
    private boolean isConnected;


    @Override
    public void onReceive(Context context, Intent intent) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initializeLocationManager(context);
        mContext = context;
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, 0,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, 0,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        isConnected = false;
        if(isConnectedToServer(BASE_URL, 10000)) {
            CheckLatLong();
        }
        else {
            sendNotification("Connection Lost to server!!!");
        }
 }
    private void CheckLatLong()
    {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if(Lat!=0.0)
                    isConnected = true;
                if(isConnected)
                {
                    sendMessage();
                }else {

                    sendNotification("GPS data is not available "+new Date().toString());
                    CheckLatLong();
                }
            }
        }, 2000);

    }
    private void sendMessage()
    {
        imei = SharedPreferenceHelper.getInstance().get(IMEI,null);
        //Toast.makeText(context, Lat+""+Lon, Toast.LENGTH_LONG).show();
        Long tsLong = System.currentTimeMillis()/1000;
        mTimeStamp = tsLong.toString();
        try{
// Creating new socket connection to the IP (first parameter) and its opened port (second parameter)
            Socket s = new Socket(IP_ADDRESS, PORT);
// Initialize output stream to write message to the socket stream
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            char buf[] = new char[256];
           // outMsg = "Lat:"+Lat+"\n Long:"+Lon+"\n UID:"+ SharedPreferenceHelper.getInstance().get(UID, null)+"\n IMEI:"+imei+"\n Timestamp:"+mTimeStamp+"$";
            outMsg = "LOC:"+SharedPreferenceHelper.getInstance().get(UID, null)+":"+imei+":"+Lat+":"+Lon+":"+mTimeStamp+"$";
            //Write message to stream
            out.write(outMsg);
            sendNotification("message sent on "+ new Date().toString());
            // Flush the data from the stream to indicate end of message
            out.flush();

            out.close();

            // Close the socket connection
            s.close();
        }

        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            return false;
        }
    }

    public void sendNotification(String mesg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.patelheggere.esy.es"));
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Something happened!!!");
        builder.setContentText(mesg);
       // builder.setSubText("Tap to view the website.");
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            Lat = location.getLatitude();
            Lon = location.getLongitude();
           mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
            sendNotification("GPS Disabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
            sendNotification("GPS Enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationReceiver.LocationListener[] mLocationListeners = new LocationReceiver.LocationListener[]{
            new LocationReceiver.LocationListener(LocationManager.GPS_PROVIDER),
            new LocationReceiver.LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private void initializeLocationManager(Context context) {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
