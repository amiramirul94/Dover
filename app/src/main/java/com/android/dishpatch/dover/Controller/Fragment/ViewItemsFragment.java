package com.android.dishpatch.dover.Controller.Fragment;

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
import android.widget.Toast;

import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 11/23/2016.
 */

public class ViewItemsFragment extends Fragment {


    private static final String TAG = ViewItemsFragment.class.getSimpleName() ;
    private RecyclerView mItemListRecyclerView;
    private int mOrderId;
    private List<ItemDescription> mItemList = new ArrayList<>();
    private ItemAdapter mItemAdapter;
    private static final String ORDER_ID_ARGS =ViewItemsFragment.class.getSimpleName()+"ORDER_ID_ARGS";

    public static ViewItemsFragment newInstance(int order_id) {

        Bundle args = new Bundle();

        args.putInt(ORDER_ID_ARGS,order_id);
        ViewItemsFragment fragment = new ViewItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrderId = getArguments().getInt(ORDER_ID_ARGS);

        if(Util.isNetworkAvailable(getActivity()))
        {
            new GetOrderItemsTask().execute();

        }else {
            Toast.makeText(getActivity(), R.string.internet_not_available, Toast.LENGTH_SHORT).show();
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_items,container,false);

        mItemListRecyclerView = (RecyclerView) v.findViewById(R.id.item_list_recycler_view);
        mItemListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }

    private void updateUI()
    {
        mItemList = DoverCentral.get(getActivity()).getOrderedItemList();
        mItemAdapter = new ItemAdapter();
        mItemListRecyclerView.setAdapter(mItemAdapter);
    }


    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{


        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.item_list_layout,parent,false);

            return new ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            ItemDescription itemDescription = mItemList.get(position);
            holder.bindItems(itemDescription);
        }


        @Override
        public int getItemCount() {
            return mItemList.size();
        }
    }


    private class ItemHolder extends RecyclerView.ViewHolder{


        private ImageView mItemImageView;
        private TextView mItemTextView;
        private TextView mRemarksTextView;
        private TextView mQuantityTextView;
        private TextView mPriceTextView;

        private ItemDescription mItemDescription;

        public ItemHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            mItemTextView = (TextView) itemView.findViewById(R.id.item_name_text_view);
            mRemarksTextView = (TextView) itemView.findViewById(R.id.remarks_text_view);
            mQuantityTextView = (TextView) itemView.findViewById(R.id.amount_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.price_text_view);
        }


        private void bindItems(ItemDescription itemDescription)
        {
            mItemDescription = itemDescription;

            if(mItemDescription.getItem().getPictureUrl()!=null)
            {
                Picasso.with(getActivity()).load(mItemDescription.getItem().getPictureUrl()).fit().into(mItemImageView);
            }

            mItemTextView.setText(mItemDescription.getItem().getItemName()+"("+ mItemDescription.getItem().getQuantity()+ mItemDescription.getItem().getUnit()+")");
            Log.v(TAG, mItemDescription.getRemarks());

            if(!mItemDescription.getRemarks().equals("null"))
            {
                mRemarksTextView.setText(mItemDescription.getRemarks());
            }else{
                mRemarksTextView.setText("No remarks");
            }

            mQuantityTextView.setText(mItemDescription.getQuantity()+"");

            String price = String.format("RM %.2f", mItemDescription.getItem().getPrice());

            mPriceTextView.setText(price);
        }
    }


    private class GetOrderItemsTask extends AsyncTask<Void,Void,List<ItemDescription>>{

        @Override
        protected List<ItemDescription> doInBackground(Void... params) {
            return DoverCentral.get(getActivity()).createOrderItemList(mOrderId);
        }

        @Override
        protected void onPostExecute(List<ItemDescription> itemDescriptions) {
            super.onPostExecute(itemDescriptions);
            updateUI();
        }
    }





}
