package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.Model.Store;
import com.android.dishpatch.dover.Service.SubmitOrderService;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.CustomerActivity;
import com.android.dishpatch.dover.Model.Item;
import com.android.dishpatch.dover.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 2/23/2016.
 */
public class ConfirmationFragment extends Fragment {

    private static final String RESTAURANT_ARGS = "RESTAURANT_ARGS";
    private static final String ORDER_LIST_ARGS = "ORDER_LIST_ARGS";
    private static final String TAG = "ConfirmationFragment";

    private RecyclerView mRecyclerView;
    private TextView mTotalPriceTextView;
    private Button mSubmitButton;

    private ConfirmationAdapter mAdapter;
    private Store mStore;
    private List<Item> mItemList;
    private List<ItemDescription> mItemDescriptionList = new ArrayList<>();

    public static ConfirmationFragment newInstance(Store store, List<Item> orderlist)
    {
        Bundle args = new Bundle();
        args.putParcelable(RESTAURANT_ARGS, store);
        args.putParcelableArrayList(ORDER_LIST_ARGS, (ArrayList<? extends Parcelable>) orderlist);


        ConfirmationFragment fragment = new ConfirmationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mStore = getArguments().getParcelable(RESTAURANT_ARGS);
        mItemList = getArguments().getParcelableArrayList(ORDER_LIST_ARGS);
        Log.i(TAG, mItemList.toString()+" Have been received");

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_confirmation,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.order_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTotalPriceTextView = (TextView) v.findViewById(R.id.total_price_text_view);
        mSubmitButton = (Button) v.findViewById(R.id.submit_order_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = CustomerActivity.newIntent(getActivity());

                JSONObject mainBody = new JSONObject();
                JSONArray menuArray = new JSONArray();
                try {
                mainBody.put("customer_uuid", DoverPreferences.getPrefUserUUID(getActivity())+"");
                mainBody.put("store_id", mStore.getId()+"");

                    Date date = new Date();
                    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    String formattedDate = formatDate.format(date);

                    mainBody.put("date_time",formattedDate);

                    int counter=0;
                for(ItemDescription itemDescription : mItemDescriptionList)
                {

                    JSONObject menuObject = new JSONObject();

                    menuObject.put("item_id", itemDescription.getItem().getItemId()+"");
                    menuObject.put("quantity", itemDescription.getQuantity());
                    menuObject.put("remarks", itemDescription.getRemarks()+"");

                    if(itemDescription.getRemarks()!=null)
                    {
                        Log.v(TAG, itemDescription.getRemarks());

                    }
                    Log.v(TAG, mItemDescriptionList.size()+"");
                    menuArray.put(counter,menuObject);
                        counter++;
                }


                    mainBody.put("orders",menuArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v(TAG,mainBody.toString());



                submitOrder(mainBody);
                startActivity(i);

                getActivity().finish();
            }
        });



        updateUI();

        return v;
    }

    private void submitOrder(JSONObject menuObject){



        Intent i = SubmitOrderService.newIntent(getActivity(),menuObject);
        getActivity().startService(i);

    }



    private void updateUI()
    {
        float totalPrice=0;

        for(int i = 0; i< mItemList.size(); i++)
        {
            totalPrice+= mItemList.get(i).getPrice();
        }

        mAdapter = new ConfirmationAdapter(mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mTotalPriceTextView.setText("RM "+totalPrice);
    }

    private class ConfirmationAdapter extends RecyclerView.Adapter<ConfirmationHolder>
    {
        private List<Item> mFoodList;

        public ConfirmationAdapter(List<Item> order)
        {
            mFoodList = order;
        }

        @Override
        public ConfirmationHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.order_list_item,parent,false);

            return new ConfirmationHolder(v);
        }

        @Override
        public void onBindViewHolder(ConfirmationHolder holder, int position) {
            Item item = mFoodList.get(position);
            holder.bindOrder(item);
        }

        @Override
        public int getItemCount() {
            return mFoodList.size();
        }
    }

    private class ConfirmationHolder extends RecyclerView.ViewHolder{
        private TextView mFoodNameTextView;
        private TextView mFoodPriceTextView;
        private ImageView mFoodImageView;
        private ImageButton mDeleteOrderImageButton;
        private ImageButton mDecreaseQuantityImageButton;
        private ImageButton mIncreaseQuantityImageButton;
        private TextView mQuantityTextView;
        private EditText mRemarksEditText;
        private int quantity = 1;

        private Item mItem;
        private ItemDescription mItemDescription;
        public ConfirmationHolder(View itemView)
        {
            super(itemView);

            mFoodNameTextView = (TextView) itemView.findViewById(R.id.order_name_text_view);
            mFoodPriceTextView = (TextView) itemView.findViewById(R.id.order_price_text_view);
            mFoodImageView = (ImageView) itemView.findViewById(R.id.order_food_image_view);
            mDeleteOrderImageButton = (ImageButton) itemView.findViewById(R.id.delete_order_image_button);
            mDecreaseQuantityImageButton = (ImageButton) itemView.findViewById(R.id.decrease_quantity_image_button);
            mIncreaseQuantityImageButton = (ImageButton) itemView.findViewById(R.id.increase_quantity_image_button);
            mQuantityTextView = (TextView) itemView.findViewById(R.id.quantity_text_view);
            mRemarksEditText = (EditText) itemView.findViewById(R.id.remarks_edit_text);

            mDeleteOrderImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemList.remove(mItem);
                    mItemDescriptionList.remove(mItemDescription);
                    int i= getAdapterPosition();

                    mAdapter.notifyDataSetChanged();

                    //updateUI();
                }
            });

            if(quantity==1)
            {
                mDecreaseQuantityImageButton.setEnabled(false);
                mIncreaseQuantityImageButton.setEnabled(true);
            }else if(quantity>1&&quantity<10)
            {

            }else if(quantity==10){
                mIncreaseQuantityImageButton.setEnabled(false);
                mDecreaseQuantityImageButton.setEnabled(true);
            }


            mIncreaseQuantityImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(quantity<10&&quantity>=1)
                    {
                        quantity++;
                        mDecreaseQuantityImageButton.setEnabled(true);
                        mIncreaseQuantityImageButton.setEnabled(true);
                    }else if(quantity==10){
                        mIncreaseQuantityImageButton.setEnabled(false);
                        mDecreaseQuantityImageButton.setEnabled(true);

                    }
                    mQuantityTextView.setText(quantity+"");
                    mItemDescription.setQuantity(quantity);

                }
            });


            mDecreaseQuantityImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(quantity>1&&quantity<=10)
                    {
                        quantity--;
                        mDecreaseQuantityImageButton.setEnabled(true);

                        mIncreaseQuantityImageButton.setEnabled(true);
                    }else{
                        mDecreaseQuantityImageButton.setEnabled(false);

                    }
                    mQuantityTextView.setText(quantity+"");
                    mItemDescription.setQuantity(quantity);

                }
            });

            mRemarksEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mItemDescription.setRemarks(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        }

        private void bindOrder(Item item)
        {
            mItem = item;

            mItemDescription = new ItemDescription(mItem,quantity);

            mItemDescriptionList.add(mItemDescription);
            mFoodNameTextView.setText(mItem.getItemName());
            String priceText = String.format("RM%.2f", mItem.getPrice());
            mFoodPriceTextView.setText(priceText);

            if(mItem.getPictureUrl()!=null&&(!mItem.getPictureUrl().isEmpty()))
            {
                Picasso.with(getActivity()).load(mItem.getPictureUrl()).fit().centerCrop().into(mFoodImageView);
            }
        }
    }



}
