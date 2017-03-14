package com.android.dishpatch.dover.Controller.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Store;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.SelectItemActivity;
import com.android.dishpatch.dover.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lenovo on 2/16/2016.
 */
public class StoreListFragment extends Fragment {

    private static final String TAG =  StoreListFragment.class.getSimpleName();
    private static final int REQUEST_READ_PHONE_STATE = 2;
    private Location mLocation;

    private static final int LOCATION_PERMISSION = 1;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private StoreAdapter mAdapter;
    private List<Store> mStoreList = new ArrayList<>();
    private List<String> keylist = new ArrayList<>();
    private static final String DISTANCE_METRIC_API_KEY = "AIzaSyBVidJAlfyK1cHXP1HdgOG5kuVjFnGLWUw";
    private static final String DISTANCE_METRIC_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&";
    private GeoFire mStoreGeofire;
    private GeoQuery mStoreGeoQuery;

    private GoogleApiClient mClient;

    public static StoreListFragment newInstance()
    {
        return new StoreListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
                    public void onConnected(@Nullable Bundle bundle) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);


                            } else if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                                getLocation();
                            }
                        } else {
                            getLocation();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();








    }




    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_restaurant_list,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.restaurant_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);



        return v;

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case LOCATION_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Permission granted");

                }
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        new GetStoresTask().execute();
    }

    private void getLocation(){
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG,"location:" + location);
                    mLocation = location;
                    queryNearestStore();
                    setCustomerLocation();
                }


            });
        }catch(SecurityException e){

            Toast.makeText(getActivity(), "Location permission missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCustomerLocation() {
        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customer_location");

        GeoFire geoLoc = new GeoFire(customerReference);
        geoLoc.setLocation(DoverPreferences.getPrefUserUUID(getActivity()),new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()));
    }

    private void updateUI()
    {
        List<Store> stores = DoverCentral.get(getActivity()).getStoreList();

        mAdapter = new StoreAdapter(stores);
        mRecyclerView.setAdapter(mAdapter);


    }

    private void queryNearestStore()
    {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("stores");
        mStoreGeofire = new GeoFire(databaseReference);
        mStoreGeoQuery = mStoreGeofire.queryAtLocation(new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()),10.0);
        mStoreGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.v(TAG,key);
                if(!keylist.contains(key)) {
                    keylist.add(key);
                    new GetStoresTask().execute();
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                Log.v(TAG,"Geo Query Ready");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                Log.v(TAG,error.toString());

            }
        });


    }



    private class GetStoresTask extends AsyncTask<Void,Void,List<Store>>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Store> doInBackground(Void... params) {



            DoverCentral.get(getActivity()).createStoreList(keylist);
            return null;
        }

        @Override
        protected void onPostExecute(List<Store> stores) {
            super.onPostExecute(stores);
            updateUI();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class StoreAdapter extends  RecyclerView.Adapter<StoreHolder>
    {
        private List<Store> mStores;

        public StoreAdapter(List<Store> stores)
        {
            mStores = stores;
        }


        @Override
        public StoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.store_list_layout,parent,false);

            return new StoreHolder(v);
        }

        @Override
        public void onBindViewHolder(StoreHolder holder, int position) {
            Store store = mStores.get(position);
            holder.bindRestaurant(store);
        }

        @Override
        public int getItemCount() {
            return mStores.size();
        }
    }

    private class StoreHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Store mStore;
        private TextView mStoreNameTextView;
        private TextView mStoreDistanceTextView;
        private TextView mStoreRatingTextView;
        private ImageView mStoreLogoImageView;
        private DatabaseReference storeReference;
        private GeoFire mGeoFire;

        public StoreHolder(View itemView)
        {
            super(itemView);

            mStoreNameTextView = (TextView) itemView.findViewById(R.id.store_name_text_view);
            mStoreDistanceTextView = (TextView) itemView.findViewById(R.id.store_distance_text_view);
            mStoreRatingTextView = (TextView) itemView.findViewById(R.id.store_rating_text_view);
            mStoreLogoImageView = (ImageView) itemView.findViewById(R.id.store_logo_image_view);

            itemView.setOnClickListener(this);

        }

        public void bindRestaurant(Store store)
        {
            mStore = store;
            mStoreNameTextView.setText(mStore.getName());

            storeReference = FirebaseDatabase.getInstance().getReference("stores");

            mGeoFire = new GeoFire(storeReference);

            mGeoFire.getLocation(mStore.getUuid(), new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    mStore.setLatitude(location.latitude);
                    mStore.setLongitude(location.longitude);
                    Log.v(TAG,mStore.getLatitude()+","+mStore.getLongitude());

                    float[] results =new float[1];
                    Location.distanceBetween(mLocation.getLatitude(),mLocation.getLongitude(),mStore.getLatitude(),mStore.getLongitude(),results);
                    Log.v(TAG,mStore.getName()+" = "+results[0]);
                    mStore.setDistance(results[0]);
                    if(results[0]<1000)
                    {
                        String distanceString = getString(R.string.distance_in_metres,results[0]);
                        mStore.setDistanceString(distanceString);
                    }else {
                        float distanceInKm = results[0]/1000;
                        String distanceString = getString(R.string.distance_in_km,distanceInKm);
                        mStore.setDistanceString(distanceString);
                    }

                    mStoreDistanceTextView.setText(mStore.getDistanceString());
//                    if(mStore.getDistanceString()==null)
//                    {
//                        new GetDistanceAsyncTask().execute();
//                    }else{
//                        mStoreDistanceTextView.setText(mStore.getDistanceString());
//                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mStoreRatingTextView.setText(String.format("%.1f", mStore.getRating()));
            mStoreLogoImageView.setMinimumHeight(mStoreLogoImageView.getWidth());
            Picasso.with(getActivity()).load(store.getPhotoUri()).fit().centerCrop().into(mStoreLogoImageView);



        }

        @Override
        public void onClick(View v) {
          Intent i = SelectItemActivity.newIntent(getActivity(), mStore);
            startActivity(i);
        }


    }


}
