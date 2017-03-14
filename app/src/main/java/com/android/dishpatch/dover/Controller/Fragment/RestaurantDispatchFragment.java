package com.android.dishpatch.dover.Controller.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.dishpatch.dover.Model.Runner;
import com.android.dishpatch.dover.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 4/28/2016.
 */
public class RestaurantDispatchFragment extends Fragment {


    private RecyclerView mDispatchRecyclerView;
    private List<Runner> mRunnerList = new ArrayList<>();
    private DispatchAdapter mDispatchAdapter;


    public static RestaurantDispatchFragment newInstance()
    {
        return new RestaurantDispatchFragment();


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for(int i=0; i<3; i++)
        {
            Runner runner = new Runner();

            mRunnerList.add(runner);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_recycler_view,container,false);
        mDispatchRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mDispatchRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }

    private void updateUI()
    {
        mDispatchAdapter = new DispatchAdapter();
        mDispatchRecyclerView.setAdapter(mDispatchAdapter);
    }


    private class DispatchAdapter extends RecyclerView.Adapter<DispatchManagerRecyclerView>{

        @Override
        public DispatchManagerRecyclerView onCreateViewHolder(ViewGroup parent, int viewType) {


            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v  = inflater.inflate(R.layout.dispatch_list_item_layout,parent,false);

            return new DispatchManagerRecyclerView(v);
        }

        @Override
        public void onBindViewHolder(DispatchManagerRecyclerView holder, int position) {
            holder.bindView(mRunnerList.get(position));
        }

        @Override
        public int getItemCount() {
            return mRunnerList.size();
        }
    }

    private class DispatchManagerRecyclerView extends RecyclerView.ViewHolder {

        TextView mDispatchNameTextView;
        TextView mAverageTimeTextView;
        Runner mRunner;


        public DispatchManagerRecyclerView(View itemView) {
            super(itemView);

            mDispatchNameTextView = (TextView) itemView.findViewById(R.id.dispatcher_name);
            mAverageTimeTextView = (TextView) itemView.findViewById(R.id.average_delivery_time);
        }

        public void bindView(Runner runner)
        {
            mRunner = runner;
            mDispatchNameTextView.setText(mRunner.getDispatcherName());
            String avg = String.format("Average delivery time : %d min", mRunner.getAverageResponseTime());
            mAverageTimeTextView.setText(avg);
        }




    }






}
