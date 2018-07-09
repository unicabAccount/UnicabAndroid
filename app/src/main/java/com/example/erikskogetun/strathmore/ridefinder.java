package com.example.erikskogetun.strathmore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.auth.api.Auth;
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
    List<Marker> markers;
    ArrayList<LatLng> locationlist;

    GoogleMap map;

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

        // TO-DO
        // This will be need to be fetched by the API
        LatLng you = new LatLng(-1.264347, 36.801293);
        LatLng personA = new LatLng(-1.274258, 36.799115);
        LatLng personB = new LatLng(-1.292725, 36.787362);
        LatLng strathmore = new LatLng(-1.309888, 36.812774);

        locationlist = new ArrayList<>();
        locationlist.add(you);
        locationlist.add(personA);
        locationlist.add(personB);
        locationlist.add(strathmore);

        requestButton = findViewById(R.id.requestUber);
        config = new SessionConfiguration.Builder()
                .setClientId("YvYcjHB9GQDfL1n8xigviNvM1b_0FQKL")
                .setRedirectUri("https://facebook.com")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.REQUEST))
                .build();
        UberSdk.initialize(config);
        RideParameters rideParams = new RideParameters.Builder()
                .setDropoffLocation(locationlist.get(locationlist.size() - 1).latitude, locationlist.get(locationlist.size() - 1).longitude, "Strathmore University", "Madaraka Estate, Ole Sangale Rd, Nairobi, Kenya")
                .setPickupLocation(locationlist.get(0).latitude, locationlist.get(0).longitude, "Home", "Home address")
                .build();
        requestButton.setRideParameters(rideParams);
        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);
        requestButton.loadRideInformation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        markers = new ArrayList<>();

        IconGenerator icg = new IconGenerator(this);
        Bitmap bm;

        for (int i = 0; i < locationlist.size(); i++) {
            if (i == 0) {
                bm = icg.makeIcon("Your home");
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(BitmapDescriptorFactory.fromBitmap(bm))));
            } else if (i == locationlist.size() - 1) {
                Drawable circleDrawable = getResources().getDrawable(R.drawable.circle_shape);
                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(circleDrawable.getIntrinsicWidth(), circleDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                circleDrawable.setBounds(0, 0, circleDrawable.getIntrinsicWidth(), circleDrawable.getIntrinsicHeight());
                circleDrawable.draw(canvas);
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(markerIcon)));
            } else {
                bm = icg.makeIcon("Person " + String.valueOf(i));
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(BitmapDescriptorFactory.fromBitmap(bm))));
            }
        }

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLngBounds.Builder b = new LatLngBounds.Builder();
                for (Marker m : markers) {
                    b.include(m.getPosition());
                }

                LatLngBounds bounds = b.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100); // bounds & padding
                map.animateCamera(cu);
            }
        });

        GoogleDirection.withServerKey("AIzaSyBH9WJEpCaMWUlQo3a9P4YU-uAwhxJJHgQ")
                .from(locationlist.get(0))
                .and(locationlist.get(1))
                .and(locationlist.get(2))
                .to(locationlist.get(3))
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
                            Log.w("Route", direction.getErrorMessage());
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e("Route", "Dafoq", t);
                    }
                });
    }

    private void setEstimates(String rawbody) {
        if (rawbody != null) {
            try {
                TextView costEstimate = findViewById(R.id.costEstimate);
                TextView timeEstmate = findViewById(R.id.timeEstmate);
                TextView personOneEstimate = findViewById(R.id.personOneEstimate);
                TextView personTwoEstimate = findViewById(R.id.personTwoEstimate);
                TextView strathmoreEstimate = findViewById(R.id.strathmoreEstimate);

                float totalDuration = 0;
                float totalDistance = 0;
                float totalFare;
                float currentDuration = 0;

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
                    totalDuration += Float.parseFloat(duration.split(" ")[0]);
                    currentDuration += Float.parseFloat(duration.split(" ")[0]);

                    if (i == 0 && legs.length() != 1) {
                        personOneEstimate.setText((int)currentDuration + " minutes to person one.");
                    } else if (i == legs.length() - 1) {
                        strathmoreEstimate.setText((int)currentDuration + " minutes to Strathmore");
                    } else {
                        personTwoEstimate.setText((int)currentDuration + " minutes to person two");
                    }
                }
                totalFare = 100 + totalDistance * 42 + totalDuration * 3; // Uber Base Rates

                costEstimate.setText("Estimated price: KSH " + (int)totalFare);
                timeEstmate.setText("Estimated time: " + (int)totalDuration + " minutes");

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
        String smsNumber = "255655846082";

        if (view.getTag().toString().equals("one")){
            smsNumber = "64733681101";

        } else if (view.getTag().toString().equals("two")){
            smsNumber = "254702632614";

        }

        Uri uri = Uri.parse("smsto:" + smsNumber);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(i, ""));
    }
}