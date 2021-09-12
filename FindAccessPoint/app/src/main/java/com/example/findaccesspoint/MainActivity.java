package com.example.findaccesspoint;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import android.content.DialogInterface;
import android.location.Location;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateWifiDataMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import javax.annotation.Nonnull;
import type.CreateWifiDataInput;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    //private ListView listView;
    private TextView textView;
    private Button buttonScan;
    private Button buttonStop;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();

    Timer timer;
    TimerTask timerTask;

    final Handler handler = new Handler();
    private String locationGPS = "";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // we link the start and stop buttons to two variables: buttonScan and buttonStop

        buttonScan = findViewById(R.id.button2);
        buttonStop = findViewById(R.id.button3);

        //start the AWSAppSync
        ClientFactory.init(this);

        // ask for permissions for location 
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        // setup a listener to the start button
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if the start button was clicked we set the visibility for this button to invisible and make visible the stop one
                buttonScan.setVisibility(View.INVISIBLE);
                buttonStop.setVisibility(View.VISIBLE);

                startTimer(10000);
                getGPS();
                scanWifi();

 }} );


        // the listener for the stop button, similar to start button's listener
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStop.setVisibility(View.INVISIBLE);
                timer.cancel();
                buttonScan.setVisibility(View.VISIBLE);
            }
        });

        // we link the textView where the scanned wifis are listed and displayed 
        textView = findViewById(R.id.textView);
        // we use a wifimanager to access the wifi service 
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // check and send notice if the wifi is not enabled 
        if (!wifiManager.isWifiEnabled()){
            Toast.makeText(this, "Please enable the WiFi to be able to use this app", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        // request for permissions 
        getLocationPermissions();
    }

    private void getGPS() // calls the GPSlocation class and gets gps info
    {
        GPSlocation gps = new GPSlocation(getApplicationContext());
        Location location = gps.getLocation();
        if(location == null) // check fine location permission is null and display warning text
        {
            Toast.makeText(getApplicationContext(),"Unable to get GPS value",Toast.LENGTH_SHORT).show();
        } else {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Toast.makeText(getApplicationContext(),"GPS Latitude = "+latitude+"\nGPS Longitude = "+longitude,Toast.LENGTH_SHORT).show();

            //need to insert it to the database
            locationGPS = latitude + " " + longitude;
        }

    }

    public void startTimer(int frequency) {
        //set a new Timer
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, frequency); //
    }

      public void initializeTimerTask() {

          timerTask = new TimerTask() {
              public void run() {
                  handler.post(new Runnable() {
                      public void run() {
                          getLocationPermissions();
                          getGPS();
                      }
                  });
              }
          };
      }


      // location request for persmissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocationPermissions() {
        // if the user has not granted yet permissions for the location, we will request for it.
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                // If the user doesn't want to accept the request the application won't work
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }

    // this function starts the scanning for all the available Wifi access points and register the result
    private void scanWifi(){
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning for WiFi...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId())
        {
            case R.id.settings:
                getFrequency();
                Toast.makeText(getApplicationContext(),"settings",Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                Toast.makeText(getApplicationContext(),"Developed for:\nMobile Computing and IOT\nBirkbeck University",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFrequency()
    {
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
        inputAlert.setTitle("Logging Frequency Setting");
        inputAlert.setMessage("Submit new frequency\n(Default 10000ms):  ");
        final EditText userInput = new EditText(this);
        inputAlert.setView(userInput);
        inputAlert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInputValue = userInput.getText().toString();
                try {
                    int myNum=10000;
                    myNum = Integer.parseInt(userInputValue);
                    startTimer(myNum);
                } catch(NumberFormatException nfe) {
                    Toast.makeText(getApplicationContext(),"Not a numeric value",Toast.LENGTH_SHORT).show();
                    startTimer(10000);
                }
            }
        });
        inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
    }



    // to be able to read the registered result we need to create a broadcast receiver ( handles broadcast intents )
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        // if something has been received, the onReceive function will be called automatically
        @Override
        public void onReceive(Context context, Intent intent) {
            // we get the result of the scan from the wifiManager and unregister the intents that has been handled
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            // we look into all the results
            for(ScanResult scanResult: results){

                // add to a list the name of the Wifi access points encountered and the location of the phone. This is the list that will be used to display the nearby wifi APs
                arrayList.add(scanResult.SSID + "    " + locationGPS);

                // we need to call getConnectionInfo for more details about each Access Point, ex: RSSI
                WifiInfo info = wifiManager.getConnectionInfo();
                //adapter.notifyDataSetChanged();

                System.out.println(arrayList);

                // we call the ObjectAWS constructor and we save the object into our AWS DynamoDB database
                ObjectsAWS obj = new ObjectsAWS(scanResult.SSID, scanResult.BSSID, info.getRssi(), scanResult.frequency, locationGPS);
                System.out.println(obj.getSSID() + " " + obj.getBSSID() + " " + obj.getRSSI() + " " + obj.getFrequency() + " " + locationGPS);

                save(obj);
            }

            // display the Wifi data
            StringBuilder builder = new StringBuilder();
            for (String line : arrayList){
                builder.append(line + "\n");
            }

            textView.setText(builder.toString());

        }
    };

    // save in DynamoDB database functionality
    private void save(ObjectsAWS obj){
        // get all the information necessary
        final String SSID = obj.getSSID();
        final String BSSID = obj.getBSSID();
        final int RSSI = obj.getRSSI();
        final int frequency = obj.getFrequency();
        final String GPS = obj.getLocation();

        // we build the input used in the mutation and than add it to the mutation query
        CreateWifiDataInput input = CreateWifiDataInput.builder().sSID(SSID).bSSID(BSSID).rSSI(RSSI).frequency(frequency).gPS(GPS).build();
        CreateWifiDataMutation addWifiDataMutation = CreateWifiDataMutation.builder().input(input).build();

        // we send the mutation request through our api and treat the response in the callback
        ClientFactory.appSyncClient().mutate(addWifiDataMutation).enqueue(mutateCallback);

    }

    // treating callbacks after the database mutations
    private GraphQLCall.Callback<CreateWifiDataMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateWifiDataMutation.Data>() {
        // Usually, the callback actions are treated asynchronous, but since we use Toast, the only functionality in our case runs on the UI Thread.
        // Nevertheless, some other actions could be performed before that, and in that case, they would be treated async.

        @Override
        public void onResponse(@Nonnull final Response<CreateWifiDataMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Data has been logged", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform addWifiDataMutation", e);
                    Toast.makeText(MainActivity.this, "Failed to log data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


}
