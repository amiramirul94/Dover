package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.CustomerActivity;
import com.android.dishpatch.dover.ui.Activity.DispatchListActivity;
import com.android.dishpatch.dover.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 3/9/2016.
 */
public class MainDispatchFragment extends Fragment {

    private static final String ARG_ORDER_ID = "ARG_ORDER_ID" ;
    private static final String GET_ORDER_INFO_URL = "http://insvite.com/php/dover/get_order_info.php?order_id=";
    private static final String ACCEPT_DISPATCH_URL = "http://insvite.com/php/dover/accept_dispatch.php";
    private static final String DECLINE_DISPATCH_URL = "http://insvite.com/php/dover/decline_dispatch.php";
    private static final String TAG =  MainDispatchFragment.class.getSimpleName();


    private TextView mStoreNameTextView;
    private TextView mCustomerNameTextView;
    private TextView mTotalPriceTextView;
    private Button mAcceptDispatchButton;
    private Button mDeclineDispatchButton;
    private Integer mOrderId;
    private String mStoreName;
    private String mStoreUUID;
    private String mCustomerName;
    private String mCustomerUUID;
    private float mTotalPrice;

    public static MainDispatchFragment newInstance()
    {
        return new MainDispatchFragment();
    }

    public static MainDispatchFragment newInstance(int orderId)
    {
        MainDispatchFragment fragment = new MainDispatchFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID,orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrderId = getArguments().getInt(ARG_ORDER_ID);
        Log.v(TAG,mOrderId+"");

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.main_dispatch_fragment,container,false);
        mAcceptDispatchButton = (Button) v.findViewById(R.id.accept_dispatch);
        mDeclineDispatchButton = (Button) v.findViewById(R.id.decline_dispatch);

        mAcceptDispatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Update database to accepted
                //Update Firebase order status to accepted
//                Intent i = DispatchListActivity.newIntent(getActivity());
//                startActivity(i);
                acceptDispatch();

            }
        });


        mDeclineDispatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineDispatch();
            }
        });

        mStoreNameTextView = (TextView) v.findViewById(R.id.restaurant_name);
        mCustomerNameTextView = (TextView) v.findViewById(R.id.customer_name);
        mTotalPriceTextView = (TextView) v.findViewById(R.id.amount_required);
        new GetOrderInfoTask().execute();


        return v;
    }



    private void acceptDispatch()
    {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("order_id",mOrderId+"")
                .add("runner_uuid", DoverPreferences.getPrefUserUUID(getActivity()))
                .add("customer_uuid",mCustomerUUID)
                .build();

        Request request = new Request.Builder().url(ACCEPT_DISPATCH_URL).post(requestBody).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful())
                {
                    Log.v(TAG,"ACCEPTED");

                    DatabaseReference orderStatus = FirebaseDatabase.getInstance().getReference("order_status");
                    orderStatus.child(mOrderId+"").setValue("received");
                    Intent i = CustomerActivity.newIntent(getActivity());
                    startActivity(i);

                    getActivity().finish();
                }
            }
        });
    }

    private void declineDispatch()
    {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("order_id",mOrderId+"")
                .add("runner_uuid", DoverPreferences.getPrefUserUUID(getActivity()))
                .add("customer_uuid",mCustomerUUID)
                .build();

        Request request = new Request.Builder().url(DECLINE_DISPATCH_URL).post(requestBody).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful())
                {
                    Log.v(TAG,"DECLINED");
                    Intent i = CustomerActivity.newIntent(getActivity());
                    startActivity(i);

                    getActivity().finish();

                }
            }
        });
    }

    private class GetOrderInfoTask extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... params) {

            getOrderInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mStoreNameTextView.setText(mStoreName);
            mCustomerNameTextView.setText(mCustomerName);
            String price = String.format("RM %.2f",mTotalPrice);
            mTotalPriceTextView.setText(price);

        }

        private void getOrderInfo()
        {
            OkHttpClient client = new OkHttpClient();

            String get = GET_ORDER_INFO_URL+mOrderId;


            Request request = new Request.Builder().url(get).build();

            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if(response.isSuccessful())
                {
                    String info = response.body().string();

                    Log.v(TAG,info);

                    try {
                        JSONObject jsonObject = new JSONObject(info);
                        JSONObject storeObject = jsonObject.getJSONObject("store");
                        mStoreName = storeObject.getString("store_name");
                        mStoreUUID = storeObject.getString("store_uuid");

                        JSONObject customerObject = jsonObject.getJSONObject("customer");
                        mCustomerName = customerObject.getString("customer_name");
                        mCustomerUUID = customerObject.getString("customer_uuid");

                        JSONObject order_object = jsonObject.getJSONObject("order");
                        mTotalPrice = Float.parseFloat(order_object.getString("total_price"));

                    }catch (JSONException e) {
                        Log.e(TAG,"Json exception",e);
                    }
                }else{
                    Log.v(TAG,"IS NOT SUCCESFUL");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }





}
