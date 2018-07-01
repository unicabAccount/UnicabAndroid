package com.example.erikskogetun.strathmore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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
import com.uber.sdk.rides.client.model.PriceEstimate;
import com.uber.sdk.rides.client.model.TimeEstimate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ridefinder extends AppCompatActivity implements OnMapReadyCallback {

    RideRequestButton requestButton;
    SessionConfiguration config;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ridefinder);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        List<Marker> markers = new ArrayList<>();
        IconGenerator icg = new IconGenerator(this);
        Bitmap bm;

        // TO-DO
        // This will be need to be fetched by the API
        LatLng you = new LatLng(-1.264347, 36.801293);
        LatLng personA = new LatLng(-1.274258, 36.799115);
        LatLng personB = new LatLng(-1.292725, 36.787362);
        LatLng strathmore = new LatLng(-1.309888, 36.812774);

        ArrayList<LatLng> locationlist = new ArrayList<>();
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
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(locationlist.get(locationlist.size() - 1).latitude, locationlist.get(locationlist.size() - 1).longitude, "Strathmore University", "Madaraka Estate, Ole Sangale Rd, Nairobi, Kenya")
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation(locationlist.get(0).latitude, locationlist.get(0).longitude, "Home", "Ring Road Kileleshwa, Nairobi, Kenya")
                .build();
        requestButton.setRideParameters(rideParams);
        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);
        requestButton.loadRideInformation();

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
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(markerIcon)));
            } else {
                bm = icg.makeIcon("Person " + String.valueOf(i));
                markers.add(map.addMarker(new MarkerOptions().position(locationlist.get(i)).icon(BitmapDescriptorFactory.fromBitmap(bm))));
            }
        }

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (Marker m : markers) {
            b.include(m.getPosition());
        }

        LatLngBounds bounds = b.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150); // bounds & padding
        map.animateCamera(cu);

        GoogleDirection.withServerKey("AIzaSyBH9WJEpCaMWUlQo3a9P4YU-uAwhxJJHgQ")
                .from(you)
                .and(personA)
                .and(personB)
                .to(strathmore)
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
                    currentDuration = Float.parseFloat(duration.split(" ")[0]);

                    if (i == 0 && legs.length() != 1) {
                        personOneEstimate.setText(currentDuration + " minutes to person one.");
                    } else if (i == legs.length() - 1) {
                        strathmoreEstimate.setText(currentDuration + " minutes to Strathmore");
                    } else {
                        personTwoEstimate.setText(currentDuration + " minutes to person two");
                    }
                }

                totalFare = 100 + totalDistance * 42 + totalDuration * 3; // Uber Base Rates

                costEstimate.setText("Estimated total price: KSH" + totalFare);
                timeEstmate.setText("Estimated total time: " + totalDuration + " minutes.");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}