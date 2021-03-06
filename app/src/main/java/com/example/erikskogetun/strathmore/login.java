package com.example.erikskogetun.strathmore;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class login extends AppCompatActivity implements View.OnClickListener {

    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN;
    GoogleSignInAccount account;
    GoogleApiClient mGoogleApiClient;
    RequestQueue queue;
    String url;

    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        RC_SIGN_IN = 1337;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            if (account.getEmail().split("@")[1].equals("strathmore.edu")) {
                updateUI(account);
            } else {
                Toast.makeText(this, "You need to log in with a strathmore email, try to sign in again", Toast.LENGTH_SHORT).show();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                mGoogleApiClient.disconnect();
                                mGoogleApiClient.connect();
                            }
                        });
            }
        } catch (ApiException e) {
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount googleSignInAccount) {
        if (googleSignInAccount == null) {
            Toast.makeText(this, "Could not login", Toast.LENGTH_SHORT).show();
        } else {
            queue = Volley.newRequestQueue(this);
            url = "http://206.189.174.133/api/userInDatabase/?email=" + googleSignInAccount.getEmail();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // The user exists and may enter
                            startRidefinder();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 404) {
                        // TO-DO
                        // The user does not exist in the database, so needs to be added
                        // Insert code for adding to database by POST
                        newUser();

                    }
                }
            });
            queue.add(stringRequest);
        }
    }

    private void newUser(){
        // TO-DO
        // Open up a custom dialogue which allows the user to input their name, last name profile picture and home location.

        //final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setCancelable(false);
        //dialog.setContentView(R.layout.new_user_dialog);
        //dialog.show();

        postUser();
    }

    private void postUser(){
        url = "http://206.189.174.133/api/userInDatabase/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // The user has successfully been added to the backend
                        startRidefinder();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", error.networkResponse.headers.toString());
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Looper.prepare();
                Map<String, String> params = new HashMap<>();
                String location[] = getRandomCoordinates();

                // Gmail account does not initially have "givenName" & "familyName" for strathmore emails
                params.put("email", account.getEmail());

                // Randomize coordinates or use google places autocomplete
                params.put("home_latitude", location[0]);
                params.put("home_longitude", location[1]);
                params.put("nr_of_trips_done", "0");

                return params;
            }
        };

        queue.add(postRequest);

    }

    private String[] getRandomCoordinates(){
        //Mellan LAT: -1.457733 & -1.137839;
        //Mellan LNG: 36.587605 & 36.972126;
        double minLat = -1.457733;
        double maxLat = -1.137839;
        double minLng = 36.587605;
        double maxLng = 36.972126;

        Random r = new Random();
        double randomLat = minLat + (maxLat - minLat) * r.nextDouble();
        double randomLng = minLng + (maxLng - minLng) * r.nextDouble();

        return new String[] {String.format("%.6g%n", randomLat), String.format("%.7g%n", randomLng)};
    }
    private void startRidefinder(){
        Intent myIntent = new Intent(this, ridefinder.class);
        myIntent.putExtra("email", account.getEmail()); //Put your id to your next Intent
        startActivity(myIntent);
    }
}

