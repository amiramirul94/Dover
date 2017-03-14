package com.android.dishpatch.dover.Controller.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Item;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.StoreEditItemActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 5/6/2016.
 */
public class StoreManageItemFragment extends Fragment {

    private static final String TAG = StoreManageItemFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MenuAdapter mMenuAdapter;
    private FloatingActionButton mAddMenuFloatingActionButton;
    private static final int REQUEST_DATA_CHANGED=0;
    private static final String UPDATE_AVAILABILITY_URL = "http://insvite.com/php/update_food_availability.php";
    List<Item> mItems = new ArrayList<>();
    private String userId;
    private boolean isSuccessful;

    public static StoreManageItemFragment newInstance()
    {
        return new StoreManageItemFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = DoverPreferences.getPrefStoreUUID(getActivity());

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }




    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_manage_menu,container,false);


        mRecyclerView = (RecyclerView) v.findViewById(R.id.menu_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddMenuFloatingActionButton = (FloatingActionButton) v.findViewById(R.id.add_menu_fab);
        mAddMenuFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = StoreEditItemActivity.newIntent(getActivity(),-1);
                startActivityForResult(i,REQUEST_DATA_CHANGED);
            }
        });

        if(Util.isNetworkAvailable(getActivity()))
        {
            new GetMenuTask().execute();
        }else{
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
        }


        updateUI();


        return v;



    }

    private void updateUI() {


        DoverCentral doverCentral = DoverCentral.get(getActivity());
        mItems = doverCentral.getItemList();

        Log.v(TAG, mItems.size()+"");

            mMenuAdapter = new MenuAdapter(mItems);
            mRecyclerView.setAdapter(mMenuAdapter);




    }



    @Override
    public void onResume() {
        super.onResume();
        if(Util.isNetworkAvailable(getActivity()))
        {
            new GetMenuTask().execute();
        }else{
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
        }
        updateUI();
    }


    @Override
    public void onPause() {
        super.onPause();
        mMenuAdapter.notifyDataSetChanged();
    }

    private class GetMenuTask extends AsyncTask<Void,Void,List<Item>>{

        @Override
        protected List<Item> doInBackground(Void... params) {


            List<Item> item = DoverCentral.get(getActivity()).createMenuList(userId);
            while (item.isEmpty()){}
            return item;
        }

        @Override
        protected void onPostExecute(List<Item> item) {

            Log.v(TAG,"On post execute item="+ item.size());
            mItems = item;
            updateUI();
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder>{

        private List<Item> mItems;

        public MenuAdapter(List<Item> items)
        {
            mItems = items;
        }
        @Override
        public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.manage_menu_list_item_layout,parent,false);
            return new MenuViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MenuViewHolder holder, int position) {
            Item item = mItems.get(position);
            holder.bindMenu(item);
        }



        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode!= Activity.RESULT_OK)
        {
            return;
        }

        if(requestCode==REQUEST_DATA_CHANGED)
        {
            if(data==null)
            {
                return;
            }else{
                Boolean result;
                result= StoreEditItemFragment.wasSaved(data);
                Log.v(TAG,result.toString());
            }
        }

    }

    private class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Item mItem;
        private ImageView mMenuImageView;
        private TextView mMenuNameTextView;
        private TextView mMenuPriceTextView;
        private Switch mMenuAvailabilitySwitch;
        private TextView mMenuAvailabilityTextView;
        private Boolean mAvailability;
        private DatabaseReference itemReference;
        private ValueEventListener mAvailabilityEventListener;

        public MenuViewHolder(View itemView) {
            super(itemView);

            mMenuImageView = (ImageView) itemView.findViewById(R.id.menu_image_view);
            mMenuNameTextView = (TextView) itemView.findViewById(R.id.menu_name_text_view);
            mMenuPriceTextView = (TextView) itemView.findViewById(R.id.price_text_view);
            mMenuAvailabilitySwitch = (Switch) itemView.findViewById(R.id.availability_switch);
            mMenuAvailabilityTextView = (TextView) itemView.findViewById(R.id.availability_text_view);





            itemView.setOnClickListener(this);
        }

        public void bindMenu(Item item)
        {

            mItem = item;

            String menuName = mItem.getItemName()+"("+mItem.getQuantity()+mItem.getUnit()+")";

            mMenuNameTextView.setText(menuName);
            String price = String.format("RM%.2f", mItem.getPrice());
            mMenuPriceTextView.setText(price);



            //Get the availability of the item
            itemReference = FirebaseDatabase.getInstance().getReference("items").child(mItem.getItemId()+"");

            mAvailabilityEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {

                        Log.v(TAG,dataSnapshot.getValue().toString());
                        Boolean isAvail= Boolean.valueOf((String) dataSnapshot.getValue());



                        mItem.setAvailable(isAvail);

                        Log.v(TAG,mItem.getItemId()+" = "+dataSnapshot.getValue());

                        //mItem.setAvailable(isAvail);
                        mMenuAvailabilitySwitch.setChecked(mItem.getAvailable());
                        if(mItem.getAvailable())
                        {
                            mMenuAvailabilityTextView.setText("Available");
                        }else{
                            mMenuAvailabilityTextView.setText("Unavailable");
                        }
                    }




            }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            itemReference.addListenerForSingleValueEvent(mAvailabilityEventListener);





            mMenuAvailabilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Log.v(TAG, mItem.getItemId()+"");
                    itemReference.setValue(isChecked+"");


                        if(isChecked){

                            mMenuAvailabilityTextView.setText("Available");
                            mItem.setAvailable(isChecked);
                        }else{
                            mMenuAvailabilityTextView.setText("Unavailable");
                            mItem.setAvailable(isChecked);
                        }



                }
            });

            if(mItem.getPictureUrl()!=null){

                if(!mItem.getPictureUrl().isEmpty())
                {
                    Log.v(TAG,"Picture url = "+ mItem.getPictureUrl());
                    Picasso.with(getActivity()).load(item.getPictureUrl()).fit().centerCrop().into(mMenuImageView);
                }

            }



        }


        @Override
        public void onClick(View v) {
            Intent intent = StoreEditItemActivity.newIntent(getActivity(), mItem.getItemId());
            startActivityForResult(intent,REQUEST_DATA_CHANGED);
        }


    }



}
