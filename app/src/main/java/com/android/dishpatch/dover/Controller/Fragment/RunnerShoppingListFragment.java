package com.android.dishpatch.dover.Controller.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 12/1/2016.
 */

public class RunnerShoppingListFragment extends Fragment {


    private RecyclerView mShoppingListRecyclerView;
    private ItemAdapter mShoppingItemAdapter;

    private List<ItemDescription> mItemList = new ArrayList<>();
    private int orderId;

    private static final String ORDER_ID_ARGS = RunnerShoppingListFragment.class.getSimpleName()+"order_id_args";

    public static RunnerShoppingListFragment newInstance(int order_id) {

        Bundle args = new Bundle();

        args.putInt(ORDER_ID_ARGS,order_id);

        RunnerShoppingListFragment fragment = new RunnerShoppingListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orderId = getArguments().getInt(ORDER_ID_ARGS);

        if(Util.isNetworkAvailable(getActivity()))
        {
            new GetShoppingItemsTask().execute();

        }else{
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_runner_shopping_list,container,false);


        mShoppingListRecyclerView = (RecyclerView) v.findViewById(R.id.shopping_list_recycler_view);
        mShoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return v;
    }

    private void updateUI()
    {
        mItemList = DoverCentral.get(getActivity()).getOrderedItemList();

        mShoppingItemAdapter = new ItemAdapter();
        mShoppingListRecyclerView.setAdapter(mShoppingItemAdapter);

    }


    private class GetShoppingItemsTask extends AsyncTask<Void,Void,List<ItemDescription>>{

        @Override
        protected List<ItemDescription> doInBackground(Void... params) {
            return DoverCentral.get(getActivity()).createOrderItemList(orderId);
        }

        @Override
        protected void onPostExecute(List<ItemDescription> itemDescriptions) {
            super.onPostExecute(itemDescriptions);
            updateUI();
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder>{

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.shopping_item_list_layout,parent,false);

            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            ItemDescription itemDescription = mItemList.get(position);
            holder.bindItems(itemDescription);
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }
    }






    private class ItemViewHolder extends RecyclerView.ViewHolder{

        private ImageView mItemImageView;
        private TextView mItemNameTextView;
        private TextView mItemRemarksTextView;
        private TextView mItemQuantityTextView;
        private TextView mPricePerUnitTextView;
        private CheckBox mDoneCheckBox;
        private ItemDescription mItemDescription;


        public ItemViewHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            mItemNameTextView = (TextView) itemView.findViewById(R.id.item_name_text_view);
            mItemRemarksTextView = (TextView) itemView.findViewById(R.id.remarks_text_view);
            mItemQuantityTextView = (TextView) itemView.findViewById(R.id.amount_text_view);
            mPricePerUnitTextView = (TextView) itemView.findViewById(R.id.price_text_view);
            mDoneCheckBox = (CheckBox) itemView.findViewById(R.id.done_check_box);

        }

        public void bindItems(ItemDescription itemDescription)
        {
            mItemDescription = itemDescription;
            String url = mItemDescription.getItem().getPictureUrl();
            if(!url.equals("null")&&url!=null&&!url.isEmpty())
            {
                Picasso.with(getActivity()).load(url).fit().into(mItemImageView);
            }

            String itemName = mItemDescription.getItem().getItemName()+"("+mItemDescription.getItem().getQuantity()+mItemDescription.getItem().getUnit()+")";
            mItemNameTextView.setText(itemName);

            String remarks = mItemDescription.getRemarks();

            if(!remarks.equals("null")&&remarks!=null&&!remarks.isEmpty())
            {
                mItemRemarksTextView.setText(remarks);
            }

            mItemQuantityTextView.setText(mItemDescription.getQuantity()+"");

            String price = String.format("RM %.2f",mItemDescription.getItem().getPrice());
            mPricePerUnitTextView.setText(price);

            DatabaseReference orderItemReference = FirebaseDatabase.getInstance().getReference("order_item_status").child(mItemDescription.getOrderItemId()+"");

            orderItemReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {

                        Boolean isBought = Boolean.valueOf((String) dataSnapshot.getValue());

                        mDoneCheckBox.setChecked(isBought);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    DatabaseReference orderStatus = FirebaseDatabase.getInstance()
                            .getReference("order_item_status");

                    orderStatus.child(mItemDescription.getOrderItemId()+"").setValue(isChecked+"");
                }
            });



        }
    }









}
