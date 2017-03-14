package com.android.dishpatch.dover.Controller.Fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TrackInfoFragment extends SupportMapFragment {

    private static final String TAG = "TrackInfoFragment";
    private static final String EXTRA_RUNNER_UUID = TrackInfoFragment.class.getSimpleName()+"EXTRA_RUNNER_UUID";
    private static final String EXTRA_STORE_UUID = TrackInfoFragment.class.getCanonicalName()+"EXTRA_STORE_UUID";

    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private static final int LOCATION_PERMISSION = 1;
    private String mRunnerUUID;
    private String mStoreUUID;
    private DatabaseReference mRunnerReference;
    private GeoFire mRunnerGeoFire;
    private DatabaseReference mStoreReference;
    private GeoFire mStoreGeofire;
    private LatLng runnerLatLng = new LatLng(0,0);
    private LatLng storeLatLng = new LatLng(0,0);
    private LatLng myPoint;
    private Marker store;
    private Marker runner;
    private HashMap<String,LatLng> mLatLngMap = new HashMap<>();

    public static TrackInfoFragment newInstance()
    {
        return new TrackInfoFragment();
    }

    public static TrackInfoFragment newInstance(String runner_uuid, String store_uuid) {

        Bundle args = new Bundle();
        args.putString(EXTRA_RUNNER_UUID,runner_uuid);
        args.putString(EXTRA_STORE_UUID,store_uuid);

        TrackInfoFragment fragment = new TrackInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Util.isGpsIsEnabled(getActivity());

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }


        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        //findLocation();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);


                            } else if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                                findLocation();
                            }
                        } else {
                            findLocation();
                        }


                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;


            }
        });

        mRunnerUUID = getArguments().getString(EXTRA_RUNNER_UUID);
        mStoreUUID = getArguments().getString(EXTRA_STORE_UUID);

        mLatLngMap.put(mRunnerUUID,new LatLng(0,0));
        mLatLngMap.put(mStoreUUID,new LatLng(0,0));


    }




    @Override
    public void onStart()
    {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mClient.disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case LOCATION_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Permission granted");
                    findLocation();
                }
                return;

        }

    }

    private void findLocation()
    {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG, "Got a location fix " + location);
                    mCurrentLocation = location;
                    updateUI();

                }

            });
        }catch (SecurityException e){

        }


    }

    private void updateUI()
    {

        if(mMap==null||mCurrentLocation==null)
        {
          return;
        }


        myPoint = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());


        MarkerOptions myLocation = new MarkerOptions().position(myPoint);
        mMap.addMarker(myLocation);

        mRunnerReference = FirebaseDatabase.getInstance().getReference("runner_location");
        mRunnerGeoFire = new GeoFire(mRunnerReference);

        mRunnerGeoFire.getLocation(mRunnerUUID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                Log.v(TAG,location.toString());
                mLatLngMap.put(key,new LatLng(location.latitude,location.longitude));
                updateMarker();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStoreReference = FirebaseDatabase.getInstance().getReference("stores");

        mStoreGeofire = new GeoFire(mStoreReference);
        mStoreGeofire.getLocation(mStoreUUID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                Log.v(TAG,location.toString());
                mLatLngMap.put(key,new LatLng(location.latitude,location.longitude));
                updateMarker();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



       updateMarker();




    }

    private void updateMarker(){

        mMap.clear();

       MarkerOptions storeMarker = new MarkerOptions().position(mLatLngMap.get(mStoreUUID));
        MarkerOptions runnerMaker = new MarkerOptions().position(mLatLngMap.get(mRunnerUUID));
        MarkerOptions myLocation = new MarkerOptions().position(myPoint);
        mMap.addMarker(storeMarker);
        mMap.addMarker(runnerMaker);
        mMap.addMarker(myLocation);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(myPoint)
                .include(mLatLngMap.get(mRunnerUUID))
                .include(mLatLngMap.get(mStoreUUID))
                .build();

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
    }
}
