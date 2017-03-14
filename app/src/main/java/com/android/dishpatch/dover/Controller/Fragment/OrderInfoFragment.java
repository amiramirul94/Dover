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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Model.Order;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.OrderInfoActivity;
import com.android.dishpatch.dover.ui.Activity.TrackInfoActivity;
import com.android.dishpatch.dover.ui.Activity.ViewItemsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lenovo on 11/21/2016.
 */

public class OrderInfoFragment extends Fragment {

    private static final String ORDER_ID_ARGS = OrderInfoActivity.class.getName()+"ORDER_ID_ARGS";
    private static final String TRACK_ORDER_ARGS = OrderInfoActivity.class.getName()+"TRACK_ORDER";
    private static final String GET_ORDER_INFO = "http://insvite.com/php/dover/get_order_info.php?";
    private static final String TAG = OrderInfoFragment.class.getSimpleName();
    private int orderId;
    private String storeName;
    private String runner_name;


    private ImageView mStoreImageView;
    private ImageView mRunnerImageView;
    private TextView mStatusTextView;
    private TextView mDateTimeTextView;
    private TextView mRunnerNameTextView;
    private TextView mStoreNameSmall;
    private TextView mRunnerNameSmall;
    private Button mViewMapButton;
    private Button mViewItemsButton;
    private String storeUUID;
    private String mRunnerUUID;

    public static OrderInfoFragment newInstance(int orderId) {

        Bundle args = new Bundle();
        args.putInt(ORDER_ID_ARGS,orderId);

        OrderInfoFragment fragment = new OrderInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static OrderInfoFragment newInstance(Order order) {

        Bundle args = new Bundle();

        args.putParcelable(TRACK_ORDER_ARGS, order);

        OrderInfoFragment fragment = new OrderInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orderId = getArguments().getInt(ORDER_ID_ARGS);

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }



    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragement_order_info, container, false);

        mStoreImageView = (ImageView) v.findViewById(R.id.store_image_view);
        mRunnerImageView = (ImageView) v.findViewById(R.id.runner_image_view);
        mStatusTextView = (TextView) v.findViewById(R.id.status_text_view);
        mDateTimeTextView = (TextView) v.findViewById(R.id.date_text_view);
        mRunnerNameTextView = (TextView) v.findViewById(R.id.runner_name);
        mStoreNameSmall = (TextView) v.findViewById(R.id.store_name_small);
        mRunnerNameSmall= (TextView) v.findViewById(R.id.runner_name_small);
        mViewMapButton = (Button) v.findViewById(R.id.view_map_button);
        mViewItemsButton = (Button) v.findViewById(R.id.view_items_button);


        mViewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = TrackInfoActivity.newIntent(getActivity(),storeUUID,mRunnerUUID);
                startActivity(i);
            }
        });

        mViewItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ViewItemsActivity.newIntent(getActivity(),orderId);
                startActivity(i);
            }
        });

        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("order_status").child(orderId+"");

        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String status = (String) dataSnapshot.getValue();
                    Log.v(TAG,dataSnapshot.toString());
                    mStatusTextView.setText(status);
                    new GetOrderInfoTask().execute();


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return v;
    }


    private class GetOrderInfoTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            return getOrderInfo();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                extractInfo(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getOrderInfo() {
            OkHttpClient client = new OkHttpClient();
            String requestString = GET_ORDER_INFO+"order_id="+orderId;
            String responseString="";
            Log.v(TAG,requestString);

            Request request = new Request.Builder().url(requestString).build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful())
                {
                    responseString= response.body().string();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;

        }


        private void extractInfo(String response) throws JSONException
        {

            Log.v(TAG,response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject storeObject = jsonObject.getJSONObject("store");
            JSONObject runnerObject = jsonObject.getJSONObject("runner");
            storeName = storeObject.getString("store_name");
            storeUUID = storeObject.getString("store_uuid");
            String storePic = storeObject.getString("profile_picture");

            Log.v(TAG,storeName+ " "+storeUUID+" ");




            mStoreNameSmall.setText(storeName);
            if(storePic!=null&&!storePic.isEmpty())
            {
                Picasso.with(getActivity()).load(storeUUID).fit().into(mStoreImageView);
            }

            JSONObject orderObject = jsonObject.getJSONObject("order");
            String date_time = orderObject.getString("date_time");

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(date_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String date_string="";

            SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");

            date_string = df.format(date);

            mDateTimeTextView.setText(date_string);

            runner_name = runnerObject.getString("runner_name");
            mRunnerUUID = runnerObject.getString("runner_uuid");

            if(runner_name!=null)
            {
                mRunnerNameTextView.setText(runner_name);
                mRunnerNameSmall.setText(runner_name);
            }






        }
    }



}
