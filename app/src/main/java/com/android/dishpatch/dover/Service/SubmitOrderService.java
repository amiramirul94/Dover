package com.android.dishpatch.dover.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.Runner;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SubmitOrderService extends IntentService {

    private static final String TAG = SubmitOrderService.class.getSimpleName();
    private static final String ORDER_EXTRA = "ORDERS_EXTRA";
    private static final String SUBMIT_ORDER_URL = "http://insvite.com/php/dover/submit_order.php";
    private static final String ADD_RUNNER_QUEUE = "http://insvite.com/php/dover/add_runner_queue.php";
    private static final String GET_ORDER_ITEMS_ID = "http://insvite.com/php/dover/get_order_items_id.php?order_id=";

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private List<Runner> mRunnerIdList = new ArrayList<>();
    private String userId;
    private int orderId;
    private boolean isOnline;





    public SubmitOrderService()
    {
        super(TAG);
    }
    public SubmitOrderService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();

        mGoogleApiClient.connect();
    }

    public static Intent newIntent(Context context, JSONObject ordersObject)
    {
        Intent i = new Intent(context,SubmitOrderService.class);
        i.putExtra(ORDER_EXTRA,ordersObject.toString());

        return i;
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String orders = intent.getStringExtra(ORDER_EXTRA);
        Log.v(TAG,orders);

        /*1.search dispatchers within the area
          2. get the dispatcher id
          3. add the dispatcher id to the json object
          4. submit the json object to the server
          5. notify the restaurant
          6.
        */
        userId = DoverPreferences.getPrefUserUUID(this);
      submitOrders(orders);

    }

    private void getLocation() {
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG,"location:" + location);
                    mLocation = location;

                }


            });
        }catch(SecurityException e){

        }


    }

    private void submitOrders(String menuObject)
    {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("order_object",menuObject)
                .build();

        Request request = new Request.Builder().url(SUBMIT_ORDER_URL).post(requestBody).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful())
                {
                    String responseString = response.body().string();
                    Log.v(TAG,responseString);

                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        orderId = Integer.parseInt(jsonObject.getString("id"));

                        DatabaseReference orderStatus = FirebaseDatabase.getInstance()
                                .getReference("order_status");

                        orderStatus.child(orderId+"").setValue("dispatching");

                        new AddOrderItemsInfoTask().execute();



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getDispatchers();
                }
            }
        });
    }

    private void getDispatchers()
    {
        DatabaseReference dispatchReference = FirebaseDatabase.getInstance().getReference("runner_location");

        GeoFire geoFire = new GeoFire(dispatchReference);

        while (mLocation==null)
        {}

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()),10.0);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                String myid = DoverPreferences.getPrefUserUUID(SubmitOrderService.this);
                if(!myid.equals(key))
                {

                    addDispatcher(key,location);


                    Log.v(TAG,"dispatcher : "+key+"");
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
                Log.v(TAG,"gEO qUERY READY");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });



    }

    private void addDispatcher(final String dispatcher, final GeoLocation location)

    {
        DatabaseReference statusReference = FirebaseDatabase.getInstance().getReference("runner_status").child(dispatcher+"").child("status");



        //Add dispatcher if the dispatcher is online
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = (String) dataSnapshot.getValue();

                if(online.equals("online"))
                {
                    Runner runner = new Runner(dispatcher);


                    double distance = calculateDistance(location.latitude,location.longitude);

                    runner.setDistance(distance);

                    mRunnerIdList.add(runner);
                    new AddDispatcherAsyncTask().execute();

                    Log.v(TAG,"added "+ runner.getDispatcherId());



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        statusReference.addValueEventListener(eventListener);

    }

    private double calculateDistance(double latitude, double longitude)
    {
        float[] distance = new float[1];
        Location.distanceBetween(mLocation.getLatitude(),mLocation.getLongitude(),latitude,longitude,distance);

        Log.v(TAG,"Distance = "+distance[0]);
        return distance[0];
    }

    class AddDispatcherAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i=0;

            JSONArray dispatcherArray = new JSONArray();


            for (Runner runner : mRunnerIdList) {
                JSONObject dispatchObject = new JSONObject();

                try {
                    dispatchObject.put("order_id",orderId+"");
                    dispatchObject.put("runner_uuid", runner.getDispatcherId()+"");
                    dispatchObject.put("runner_distance", runner.getDistance()+"");
                    dispatcherArray.put(i,dispatchObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                i++;

            }

            Log.v(TAG,dispatcherArray.toString());


            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new FormBody.Builder().add("runner_object",dispatcherArray.toString()).build();

            Request request = new Request.Builder().post(requestBody).url(ADD_RUNNER_QUEUE).build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful())
                    {
                        Log.v(TAG,"Successfully added");
                    }
                }
            });




            Log.v(TAG,dispatcherArray.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mRunnerIdList.clear();
        }
    }

    private class AddOrderItemsInfoTask extends AsyncTask<Void,Void,List<Integer>>{

        @Override
        protected List<Integer> doInBackground(Void... params) {
            List<Integer> orderItemId = new ArrayList<>();

            OkHttpClient client = new OkHttpClient();

            String requestString = GET_ORDER_ITEMS_ID+orderId;

            Request request = new Request.Builder().url(requestString).build();

            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if(response.isSuccessful())
                {
                    String responseString = response.body().string();

                    JSONObject orderItem = new JSONObject(responseString);

                    JSONArray itemIdArray = orderItem.getJSONArray("items");

                    for(int i=0; i<itemIdArray.length(); i++)
                    {
                        JSONObject orderItemObj = itemIdArray.getJSONObject(i);
                        orderItemId.add(Integer.parseInt(orderItemObj.getString("order_item_id")));
                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return orderItemId;
        }


        @Override
        protected void onPostExecute(List<Integer> integers) {
            super.onPostExecute(integers);

            for(int i=0; i<integers.size(); i++)
            {
                int id = integers.get(i);

                DatabaseReference orderStatus = FirebaseDatabase.getInstance()
                        .getReference("order_item_status");

                orderStatus.child(id+"").setValue("false");
            }

        }
    }





}
