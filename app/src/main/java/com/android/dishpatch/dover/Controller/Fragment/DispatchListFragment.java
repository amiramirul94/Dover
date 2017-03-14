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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.RunnerTask;
import com.android.dishpatch.dover.Service.DispatchLocationServices;
import com.android.dishpatch.dover.ui.Activity.DispatchOrderStatusActivity;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 3/18/2016.
 */
public class DispatchListFragment extends Fragment {


    private static final String TAG = DispatchListFragment.class.getSimpleName() ;
    private List<RunnerTask> mRunnerTaskList =new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DispatchAdapter mDispatchAdapter;

    public static DispatchListFragment newInstance()
    {
        return new DispatchListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GetRunnerTask().execute();




    }

    @Override
    public void onResume() {
        super.onResume();

        Intent i = DispatchLocationServices.newIntent(getActivity());
        getActivity().startService(i);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dispatch_list,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.dispatch_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }
    private void updateUI()
    {
        mDispatchAdapter = new DispatchAdapter(mRunnerTaskList);
        mRecyclerView.setAdapter(mDispatchAdapter);
    }


    private class GetRunnerTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
           DoverCentral.get(getActivity()).createRunnerTaskList(DoverPreferences.getPrefUserUUID(getActivity()));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRunnerTaskList = DoverCentral.get(getActivity()).getRunnerTaskList();
            updateUI();
        }
    }

    private class DispatchAdapter extends RecyclerView.Adapter<DispatchList>{

        private List<RunnerTask> mRunnerTasks = new ArrayList<>();

        public DispatchAdapter(List<RunnerTask> d)
        {
            mRunnerTasks = d;
        }

        @Override
        public DispatchList onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.dispatch_order_item_layout,parent,false);

            return new DispatchList(v);
        }

        @Override
        public void onBindViewHolder(DispatchList holder, int position) {

            RunnerTask runnerTask = mRunnerTasks.get(position);
            holder.bindDispatch(runnerTask);


        }

        @Override
        public int getItemCount() {
            return mRunnerTasks.size();
        }
    }

    private class DispatchList extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mCustomerNameTextView;
        private TextView mStoreNameTextView;
        private TextView mStatusTextView;
        private RunnerTask mRunnerTask;
        private ImageView mCustomerProfilePicImageView;

        public DispatchList(View itemView)
        {
            super(itemView);

            mCustomerNameTextView = (TextView) itemView.findViewById(R.id.customer_name_text_view);
            mStoreNameTextView = (TextView) itemView.findViewById(R.id.store_name_text_view);
            mStatusTextView = (TextView) itemView.findViewById(R.id.status_text_view);
            mCustomerProfilePicImageView = (ImageView) itemView.findViewById(R.id.customer_profile_picture_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDispatch(RunnerTask runnerTask)
        {
            mRunnerTask = runnerTask;

            mCustomerNameTextView.setText(mRunnerTask.getCustomer().getName());
            mStoreNameTextView.setText(mRunnerTask.getOrder().getStore().getName());
            String profile_pic = mRunnerTask.getCustomer().getPictureUrl();

            if(!profile_pic.isEmpty()&&!profile_pic.equals("null")&&profile_pic!=null)
            {
                Picasso.with(getActivity()).load(profile_pic).fit().into(mCustomerProfilePicImageView);
            }

            DatabaseReference mOrderStatus = FirebaseDatabase.getInstance().getReference("order_status").child(mRunnerTask.getOrder().getOrderId()+"");

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

        }


        @Override
        public void onClick(View v) {

            DoverCentral.get(getActivity()).putValue("ORDER_DATA", mRunnerTask);
            Intent i = DispatchOrderStatusActivity.newIntent(getActivity(), mRunnerTask);
            startActivity(i);
        }
    }

}
