package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Item;
import com.android.dishpatch.dover.Model.Store;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.ConfirmationActivity;
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
 * Created by Lenovo on 2/17/2016.
 */
public class SelectItemFragment extends Fragment {

    private static final String ARG_RESTAURANT = "RESTAURANT";
    private static final String TAG = "SelectItemFragment";
    public static final int REQUEST_RESTAURANT = 0;

    private RecyclerView mRecyclerView;
    private SelectFoodAdapter mAdapter;
    private Store mStore;
    private List<Item> mOrderList = new ArrayList<>();
    private List<Item> mItemList = new ArrayList<>();
    private Button mDoneOrderButton;
    private Spinner mSelectCategorySpinner;
    private String mSubtitle;


    public static SelectItemFragment newInstance(Store store)
    {
        Bundle args = new Bundle();
        SelectItemFragment fragment = new SelectItemFragment();
        args.putParcelable(ARG_RESTAURANT, store);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mStore =  getArguments().getParcelable(ARG_RESTAURANT);

        updateSubtitle();

        if(Util.isNetworkAvailable(getActivity()))
        {
            new GetItemTask().execute();

        }else {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }




    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_select_item_list,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.select_food_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mDoneOrderButton = (Button) v.findViewById(R.id.done_order_button);
        mSelectCategorySpinner = (Spinner) v.findViewById(R.id.item_category_selector_spinner);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.select_grocery_type,android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectCategorySpinner.setAdapter(categoryAdapter);

        mSelectCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString();

                if(category.equals(parent.getItemAtPosition(0)))
                {
                    Log.v(TAG,category);
                    new GetItemTask().execute();
                }else {
                    Log.v(TAG,category);
                    new GetItemByCategoryTask().execute(category);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mDoneOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOrderList.isEmpty())
                {
                    Toast.makeText(getActivity(),"Please select at least one Item",Toast.LENGTH_LONG).show();
                }else{

                    Intent i= ConfirmationActivity.newIntent(getActivity(), mStore,mOrderList);
                    startActivityForResult(i,REQUEST_RESTAURANT);
                    getActivity().finish();
                }
            }
        });

        return v;
    }

    private void updateUi()
    {
        mItemList = DoverCentral.get(getActivity()).getCustomerItemList();
        mAdapter = new SelectFoodAdapter(mItemList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void updateSubtitle()
    {
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if(mOrderList.size()<=1)
        {
            mSubtitle = getString(R.string.subtitle_num_order_single,mOrderList.size()+"");
        }else if(mOrderList.size()>1)
        {
            mSubtitle = getString(R.string.subtitle_num_order_multiple,mOrderList.size()+"");
        }

        activity.getSupportActionBar().setSubtitle(mSubtitle);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_select_item_list,menu);

        MenuItem searchItem = menu.findItem(R.id.item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new SearchItemsTask().execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private class GetItemTask extends AsyncTask<Void,Void,List<Item>>{

        @Override
        protected List<Item> doInBackground(Void... params) {

            DoverCentral.get(getActivity()).createCustomerItemList(mStore.getUuid());
            return null;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            super.onPostExecute(items);
            updateUi();
        }
    }

    private class GetItemByCategoryTask extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {

            String category = params[0];
            Log.v(TAG,"category = "+category);
            DoverCentral.get(getActivity()).createCustomerItemListByCategory(category,mStore.getUuid());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUi();
        }
    }

    private class SearchItemsTask extends  AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String keyword = params[0];
            DoverCentral.get(getActivity()).createCustomerItemListBySearch(keyword,mStore.getUuid());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUi();
        }
    }

    private class SelectFoodAdapter extends RecyclerView.Adapter<SelectItemViewHolder>
    {
        private List<Item> mItems;

        public SelectFoodAdapter(List<Item> item)
        {
            mItems = item;
        }

        @Override
        public SelectItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.select_list_item,parent,false);

            return new SelectItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(SelectItemViewHolder holder, int position) {
            Item item = mItems.get(position);
            holder.bindItem(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class SelectItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView mItemNameTextView;
        private TextView mPriceTextView;
        private LinearLayout mMainLinearLayout;
        private ImageView mFoodImageView;
        private DatabaseReference mItemReference;
        private TextView mAvailabilityTextView;
        private Item mItem;

        public SelectItemViewHolder(View itemView)
        {
            super(itemView);

            mItemNameTextView = (TextView) itemView.findViewById(R.id.item_name_text_view);
            mPriceTextView = (TextView) itemView.findViewById(R.id.item_price_text_view);
            mMainLinearLayout = (LinearLayout) itemView.findViewById(R.id.container_food_list_linear_layout);
            mFoodImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            mAvailabilityTextView = (TextView) itemView.findViewById(R.id.availability_text_view);
            itemView.setOnClickListener(this);
        }

        public void bindItem(Item item)
        {
            mItem = item;



            mItemNameTextView.setText(mItem.getItemName()+"("+mItem.getQuantity()+mItem.getUnit()+")");
            String priceString = String.format("RM %.2f", mItem.getPrice());
            mPriceTextView.setText(priceString);
            mItemReference = FirebaseDatabase.getInstance().getReference("items").child(mItem.getItemId()+"");

            ValueEventListener itemAvailabilityListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {
                        Log.v(TAG,mItem.getItemId()+"="+dataSnapshot.getValue());

                        Boolean availability = Boolean.valueOf((String) dataSnapshot.getValue());

                        if(availability)
                        {
                            mAvailabilityTextView.setText("available");
                        }else{
                            mAvailabilityTextView.setText("unavailable");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };


            mItemReference.addValueEventListener(itemAvailabilityListener);


            Uri uri = Uri.parse(mItem.getPictureUrl());

            if(uri!=null)
            {
                Picasso.with(getActivity()).load(uri).centerCrop().fit().into(mFoodImageView);
            }
            if(mOrderList.contains(mItem))
            {
                mMainLinearLayout.setBackgroundResource(R.drawable.box_container_background_selected);
            }else{

                mMainLinearLayout.setBackgroundResource(R.drawable.box_container_background_normal);
            }

        }


        @Override
        public void onClick(View v) {

            //if the menu is in the order list remove and change the background to normal
            if(mOrderList.contains(mItem)){
                mOrderList.remove(mItem);
                mMainLinearLayout.setBackgroundResource(R.drawable.box_container_background_normal);
                updateSubtitle();
            }else {
                //if does not contain, add the menu in the order list and change background to selected
                mOrderList.add(mItem);
                mMainLinearLayout.setBackgroundResource(R.drawable.box_container_background_selected);
                updateSubtitle();
            }

        }
    }





}
