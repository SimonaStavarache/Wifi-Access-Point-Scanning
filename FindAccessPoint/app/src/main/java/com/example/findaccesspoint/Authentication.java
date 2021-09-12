package com.example.findaccesspoint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

public class Authentication extends AppCompatActivity {

    private final String TAG = Authentication.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //we use amplify auth to authenticate the users
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails result) {
                Log.i(TAG, result.getUserState().toString());
                switch (result.getUserState()){
                    // if the authentication has been done and the user exists, the MainActivity class is launched
                    case SIGNED_IN:
                        Intent i = new Intent(Authentication.this, MainActivity.class);
                        startActivity(i);
                        break;
                    // if the user isn't logged then the SignIn is displayed
                    case SIGNED_OUT:
                        showSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    // displays the SignIn page
    private void showSignIn(){
        try{
            AWSMobileClient.getInstance().showSignIn(this, SignInUIOptions.builder().nextActivity(MainActivity.class).build());
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }
}
