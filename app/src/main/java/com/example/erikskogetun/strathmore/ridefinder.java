package com.example.erikskogetun.strathmore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFinishedListener;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ridefinder extends AppCompatActivity implements OnMapReadyCallback {

    RideRequestButton requestButton;
    SessionConfiguration config;
    GoogleApiClient mGoogleApiClient;
    ArrayList<Marker> markers;
    ArrayList<LatLng> locationlist;
    GoogleMap map;
    RequestQueue queue;
    String userEmail;
    IconGenerator icg;
    Bitmap bm;
    String phonePerson1;
    String phonePerson2;
    RelativeLayout foundPassengersLayout;
    RelativeLayout estimatesLayout;
    String namePassengerOne;
    String namePassengerTwo;

    @Override
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
        setContentView(R.layout.activity_ridefinder);

        foundPassengersLayout = findViewById(R.id.foundPassengersLayout);
        estimatesLayout = findViewById(R.id.estimatesLayout);

        icg = new IconGenerator(this);
        markers = new ArrayList<>();
        locationlist = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        userEmail = extras.getString("email");

        TextView searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passengerSearch();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        queue = Volley.newRequestQueue(this);
        String url = "http://206.189.174.133/api/userInDatabase/?email=" + userEmail;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = (JSONObject) new JSONArray(response).get(0);
                            LatLng you = new LatLng(jsonResponse.getDouble("home_latitude"), jsonResponse.getDouble("home_longitude"));
                            locationlist.add(you);
                            firstMapSetup();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), String.valueOf(error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }
    private void firstMapSetup(){

        LatLng strathmore = new LatLng(-1.309888, 36.812774);
        locationlist.add(strathmore);

        for (int i = 0; i < locationlist.size(); i++) {
            if (i == 0) {
                bm = icg.makeIcon("Your home");
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(BitmapDescriptorFactory.fromBitmap(bm))));
            } else if (i == 1) {
                Drawable circleDrawable = getResources().getDrawable(R.drawable.circle_shape);
                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(circleDrawable.getIntrinsicWidth(), circleDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                circleDrawable.setBounds(0, 0, circleDrawable.getIntrinsicWidth(), circleDrawable.getIntrinsicHeight());
                circleDrawable.draw(canvas);
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(markerIcon)));
            }
        }

        GoogleDirection.withServerKey("AIzaSyBH9WJEpCaMWUlQo3a9P4YU-uAwhxJJHgQ")
                .from(locationlist.get(1))
                .to(locationlist.get(0))
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            setEstimates(rawBody);
                            List<Step> stepList;
                            ArrayList<PolylineOptions> polylineOptionList;
                            List<Leg> legList = direction.getRouteList().get(0).getLegList();
                            for (int i = 0; i < legList.size(); i++) {
                                stepList = legList.get(i).getStepList();
                                polylineOptionList = DirectionConverter.createTransitPolyline(getBaseContext(), stepList, 5, Color.RED, 3, Color.BLUE);
                                for (PolylineOptions polylineOption : polylineOptionList) {
                                    map.addPolyline(polylineOption);
                                }
                            }
                        } else {
                            Log.w("Route", "Cannot do directions for these addresses");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e("Route", "Dafoq", t);
                    }
                });

        requestButton = findViewById(R.id.requestUber);
        config = new SessionConfiguration.Builder()
                .setClientId("YvYcjHB9GQDfL1n8xigviNvM1b_0FQKL")
                .setRedirectUri("https://facebook.com")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.REQUEST))
                .build();
        UberSdk.initialize(config);
        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(locationlist.get(locationlist.size() - 1).latitude, locationlist.get(locationlist.size() - 1).longitude, "Strathmore University", "Madaraka Estate, Ole Sangale Rd, Nairobi, Kenya")
                .setDropoffLocation(locationlist.get(0).latitude, locationlist.get(0).longitude, "Home", "Home address")
                .build();
        requestButton.setRideParameters(rideParams);
        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);
        requestButton.loadRideInformation();

        setCamera();
    }

    private void setCamera(){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (Marker m : markers) {
            b.include(m.getPosition());
        }

        LatLngBounds bounds = b.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 140); // bounds & padding
        map.animateCamera(cu);
    }

    private void setEstimates(String rawbody) {
        if (rawbody != null) {
            try {
                TextView costEstimate = findViewById(R.id.costEstimate);
                TextView personOneEstimate = findViewById(R.id.personOneEstimate);
                TextView personTwoEstimate = findViewById(R.id.personTwoEstimate);
                TextView strathmoreEstimate = findViewById(R.id.strathmoreEstimate);

                float totalDuration = 0;
                float totalDistance = 0;
                float totalFare;

                JSONObject jsonObj = new JSONObject(rawbody);
                JSONArray routes = jsonObj.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                for (int i = 0; i < legs.length(); i++) {
                    String distance = legs.getJSONObject(i).getJSONObject("distance").get("text").toString();
                    String duration = legs.getJSONObject(i).getJSONObject("duration").get("text").toString();

                    if (distance.split(" ")[1].equals("m")) {
                        totalDistance += Float.parseFloat(distance.split(" ")[0]) / 1000;
                    } else {
                        totalDistance += Float.parseFloat(distance.split(" ")[0]);
                    }

                    if (duration.split(" ")[1].equals("hour")) {
                        totalDuration += Float.parseFloat(duration.split(" ")[0])*60;
                        totalDuration += Float.parseFloat(duration.split(" ")[2]);
                    } else {
                        totalDuration += Float.parseFloat(duration.split(" ")[0]);
                    }

                    if (i == 0 && legs.length() != 1) {
                        personOneEstimate.setText((int)totalDuration + " minutes to " + namePassengerOne + ".");
                        personOneEstimate.setTextSize(12);
                    } else if (i == legs.length() - 1) {
                        strathmoreEstimate.setText((int)totalDuration + " minutes to Strathmore.");
                    } else {
                        personTwoEstimate.setText((int)totalDuration + " minutes to " + namePassengerTwo + ".");
                        personTwoEstimate.setTextSize(12);
                    }
                }
                totalFare = 100 + totalDistance * 42 + totalDuration * 3; // Uber Base Rates

                costEstimate.setText("Estimated price: KSH " + (int)totalFare);

                ViewGroup.LayoutParams estimateParams = estimatesLayout.getLayoutParams();
                estimateParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                estimatesLayout.setLayoutParams(estimateParams);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void logOutGoogle(View view) {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
            new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                    Intent i = new Intent(getApplicationContext(), login.class);
                    startActivity(i);
                }
            });
    }

    public void whatsApp(View view){
        String smsNumber = "0";

        if (view.getTag().toString().equals("one")){
            smsNumber = phonePerson1;

        } else if (view.getTag().toString().equals("two")){
            smsNumber = phonePerson2;

        }

        Uri uri = Uri.parse("smsto:" + smsNumber);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(i, ""));
    }

    private void passengerSearch(){
        String url = "http://206.189.174.133/distanceview/" + userEmail + "/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            setDirections(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), String.valueOf(error.networkResponse.statusCode), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }

    private void setDirections(String response) throws JSONException {

        JSONArray jsonResponse = new JSONArray(response);

        Log.w("response", locationlist.toString());

        while (locationlist.size() != 2){
            locationlist.remove(2);
            markers.get(2).remove();
            markers.remove(2);
        }

        locationlist.add(new LatLng(Double.valueOf(((JSONObject)jsonResponse.get(0)).get("home_latitude").toString()), Double.valueOf(((JSONObject)jsonResponse.get(0)).get("home_longitude").toString())));
        locationlist.add(new LatLng(Double.valueOf(((JSONObject)jsonResponse.get(1)).get("home_latitude").toString()), Double.valueOf(((JSONObject)jsonResponse.get(1)).get("home_longitude").toString())));
        phonePerson1 = ((JSONObject)jsonResponse.get(0)).getString("phone_nr");
        phonePerson2 = ((JSONObject)jsonResponse.get(1)).getString("phone_nr");
        namePassengerOne = ((JSONObject)jsonResponse.get(0)).getString("first_name");
        namePassengerTwo = ((JSONObject)jsonResponse.get(1)).getString("first_name");
        ((TextView)findViewById(R.id.passengerOne)).setText(namePassengerOne);
        ((TextView)findViewById(R.id.passengerTwo)).setText(namePassengerTwo);

        for (int i = 0; i < locationlist.size(); i++) {
            String name = "";
            if (i == 2){
                name = namePassengerOne;
            }
            if (i == 3){
                name = namePassengerTwo;
            }
            if (i > 1){
                bm = icg.makeIcon(name);
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(BitmapDescriptorFactory.fromBitmap(bm))));
            }
        }

        GoogleDirection.withServerKey("AIzaSyBH9WJEpCaMWUlQo3a9P4YU-uAwhxJJHgQ")
            .from(locationlist.get(0))
            .and(locationlist.get(2))
            .and(locationlist.get(3))
            .to(locationlist.get(1))
            .transportMode(TransportMode.DRIVING)
            .execute(new DirectionCallback() {
                @Override
                public void onDirectionSuccess(Direction direction, String rawBody) {
                    if (direction.isOK()) {
                        Log.w("Route", "Is ok but will not draw??");
                        setEstimates(rawBody);
                        List<Step> stepList;
                        ArrayList<PolylineOptions> polylineOptionList;
                        List<Leg> legList = direction.getRouteList().get(0).getLegList();
                        for (int i = 0; i < legList.size(); i++) {
                            stepList = legList.get(i).getStepList();
                            polylineOptionList = DirectionConverter.createTransitPolyline(getBaseContext(), stepList, 5, Color.RED, 3, Color.BLUE); // TO-DO change "i" to Color.RED
                            for (PolylineOptions polylineOption : polylineOptionList) {
                                map.addPolyline(polylineOption);
                            }
                        }
                    } else {
                        Log.w("Route", "Cannot do directions for these addresses");
                    }
                }

                @Override
                public void onDirectionFailure(Throwable t) {
                    Log.e("Route", "Dafoq", t);
                }
            });

        setCamera();

        ViewGroup.LayoutParams params = foundPassengersLayout.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        foundPassengersLayout.setLayoutParams(params);

    }
}