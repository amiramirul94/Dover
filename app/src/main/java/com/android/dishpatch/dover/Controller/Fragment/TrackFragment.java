package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Order;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.OrderInfoActivity;
import com.android.dishpatch.dover.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 2/28/2016.
 */
public class TrackFragment extends Fragment {

    private static final String TAG =TrackFragment.class.getSimpleName() ;
    private List<Order> mOrderList = new ArrayList<>();
    private RecyclerView mTrackListRecyclerView;
    private TrackListAdapter mAdapter;


    public static TrackFragment newInstance()
    {
        return new TrackFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Util.isNetworkAvailable(getActivity()))
        {
            new TrackOrdersAsyncTask().execute();

        }else {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }




    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_recycler_view,container,false);

        mTrackListRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mTrackListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return v;
    }

    private void updateUI()
    {
        mAdapter = new TrackListAdapter(mOrderList);
        mTrackListRecyclerView.setAdapter(mAdapter);
    }



    private class TrackOrdersAsyncTask extends AsyncTask<Void,Void,List<Order>> {


        @Override
        protected List<Order> doInBackground(Void... params) {

            return DoverCentral.get(getActivity()).createTrackList(DoverPreferences.getPrefUserUUID(getActivity()));
        }

        @Override
        protected void onPostExecute(List<Order> orderList) {
            super.onPostExecute(orderList);
            mOrderList = orderList;
            updateUI();
        }
    }

    private class TrackListAdapter extends RecyclerView.Adapter<TrackListViewHolder>{

        private List<Order> mOrderList = new ArrayList<>();
        public TrackListAdapter(List<Order> orderList)
        {
            mOrderList = orderList;
        }
        @Override
        public TrackListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            View v = inflater.inflate(R.layout.track_list_item,parent,false);

            return new TrackListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TrackListViewHolder holder, int position) {

            holder.bindTrackList(mOrderList.get(position));
        }

        @Override
        public int getItemCount() {
            return mOrderList.size();
        }
    }


    private class TrackListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mStoreTextView;
        private TextView mStatusTextView;
        private TextView mDateTextView;
        private Order mOrder;

        public TrackListViewHolder(View itemView) {
            super(itemView);

            mStoreTextView = (TextView) itemView.findViewById(R.id.store_name_text_view);
            mStatusTextView = (TextView) itemView.findViewById(R.id.status_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
            itemView.setOnClickListener(this);

        }

        public void bindTrackList(Order order)
        {
            String orderText="";
            final String dateString;
            mOrder = order;


            SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");

            Date dtrack = mOrder.getDate();

            dateString = df.format(dtrack);


            mStoreTextView.setText(mOrder.getStore().getName());

            DatabaseReference mOrderStatus = FirebaseDatabase.getInstance().getReference("order_status").child(mOrder.getOrderId()+"");

            mOrderStatus.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   Log.v(TAG,dataSnapshot.toString());
                    String status = (String) dataSnapshot.getValue();
                    mStatusTextView.setText(status);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDateTextView.setText(dateString);


        }

        @Override
        public void onClick(View v) {

            Intent i = OrderInfoActivity.newIntent(getActivity(), mOrder.getOrderId());
            startActivity(i);
        }
    }
}
