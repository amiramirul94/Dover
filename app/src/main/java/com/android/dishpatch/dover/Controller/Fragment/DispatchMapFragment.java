package com.android.dishpatch.dover.Controller.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.Model.Runner;
import com.android.dishpatch.dover.Model.RunnerTask;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by Lenovo on 4/19/2016.
 */
public class DispatchMapFragment extends SupportMapFragment {

    private static final String BUNDLE_DISPATCH = "BUNDLE_DISPATCH";
    private static final String EXTRA_DISPATCH_RESULT = "com.andrid.dispatch.dispatch.DispatchFragment.Runner";


    public static Runner getDispatchResult(Intent result)
    {
        return result.getParcelableExtra(EXTRA_DISPATCH_RESULT);
    }

    public static DispatchMapFragment newInstance(RunnerTask runnerTask)
    {
        Bundle args = new Bundle();

        args.putParcelable(BUNDLE_DISPATCH, runnerTask);

        DispatchMapFragment fragment = new DispatchMapFragment();

        fragment.setArguments(args);
        return fragment;
    }


    private static final String TAG = "DispatchMapFragment";
    private static final int LOCATION_PERMISSION = 1;

    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;
    private RunnerTask mRunnerTask;
    private Location mCurrentLocation;
    private Location mStoreLocation;
    private Location mCustomerLocation;
    private LatLng mCustomerLatLng = new LatLng(0,0);
    private LatLng mStoreLatLng = new LatLng(0,0);
    private LatLng myPoint = new LatLng(0,0);
    private DatabaseReference mCustomerReference;
    private GeoFire mCustomerGeofire;
    private HashMap<String,LatLng> mLatLngMap = new HashMap<>();
    private DatabaseReference mStoreReference;
    private GeoFire mStoreGeofire;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Util.isGpsIsEnabled(getActivity());

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }


        mRunnerTask = getArguments().getParcelable(BUNDLE_DISPATCH);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
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
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
            }
        });

        mLatLngMap.put(mRunnerTask.getCustomer().getUuid(),new LatLng(0,0));
        mLatLngMap.put(mRunnerTask.getOrder().getStore().getUuid(),new LatLng(0,0));

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
        if(mGoogleMap==null||mCurrentLocation==null)
        {
            return;
        }


        myPoint = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());


        MarkerOptions myLocation = new MarkerOptions().position(myPoint);
        mGoogleMap.addMarker(myLocation);

        mCustomerReference = FirebaseDatabase.getInstance().getReference("customer_location");
        mCustomerGeofire = new GeoFire(mCustomerReference);

        mCustomerGeofire.getLocation(mRunnerTask.getCustomer().getUuid(), new LocationCallback() {
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
        mStoreGeofire.getLocation(mRunnerTask.getOrder().getStore().getUuid(), new LocationCallback() {
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

    private void updateMarker() {

        mGoogleMap.clear();

        MarkerOptions storeMarker = new MarkerOptions().position(mLatLngMap.get(mRunnerTask.getOrder().getStore().getUuid()));
        MarkerOptions customerMaker = new MarkerOptions().position(mLatLngMap.get(mRunnerTask.getCustomer().getUuid()));
        MarkerOptions myLocation = new MarkerOptions().position(myPoint);
        mGoogleMap.addMarker(storeMarker);
        mGoogleMap.addMarker(customerMaker);
        mGoogleMap.addMarker(myLocation);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(myPoint)
                .include(mLatLngMap.get(mRunnerTask.getCustomer().getUuid()))
                .include(mLatLngMap.get(mRunnerTask.getOrder().getStore().getUuid()))
                .build();

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(update);
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

    @Override
    public void onStart() {
        super.onStart();

        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mClient.disconnect();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Intent data = new Intent();
        data.putExtra(EXTRA_DISPATCH_RESULT, mRunnerTask);
        getActivity().setResult(Activity.RESULT_OK,data);

    }
}
