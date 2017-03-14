package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.net.Uri;
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

import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.Model.RunnerTask;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.DispatchMapActivity;
import com.android.dishpatch.dover.ui.Activity.RunnerShoppingListActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;


public class DispatchOrderStatusFragment extends Fragment {

    private static final String DISPATCH_ORDER_ARGS = "DISPATCH_ORDER_ARGS";
    private static final String TAG = "DispatchOrderStatus";
    private static final int REQUEST_DISPATCH = 1;


    private ImageView mStoreImageView;
    private ImageView mCustomerImageView;
    private TextView mCustomerNameTextView;
    private Button mNavigateCustomerButton;
    private TextView mStoreNameTextView;
    private Button mNavigateStoreButton;
    private TextView mAmountRequiredTextView;
    private TextView mEarningsTextView;


    private Button mMapButton;
    private Button mViewItemsButton;

    private RunnerTask mRunnerTask;

    public static DispatchOrderStatusFragment newInstance(RunnerTask runnerTask){

        Bundle args = new Bundle();
        args.putParcelable(DISPATCH_ORDER_ARGS, runnerTask);

        DispatchOrderStatusFragment fragment = new DispatchOrderStatusFragment();

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRunnerTask = getArguments().getParcelable(DISPATCH_ORDER_ARGS);
        Log.d(TAG,mRunnerTask.getCustomer().getName());

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dispatch_order_status,container,false);

        mStoreImageView = (ImageView) v.findViewById(R.id.store_profile_image_view);
        mCustomerImageView = (ImageView) v.findViewById(R.id.customer_profile_image_view);

        mCustomerNameTextView = (TextView) v.findViewById(R.id.customer_name_text_view);
        mNavigateCustomerButton = (Button) v.findViewById(R.id.navigate_to_customer);

        mStoreNameTextView = (TextView) v.findViewById(R.id.store_name_text_view);
        mNavigateStoreButton = (Button) v.findViewById(R.id.navigate_store_button);
        mAmountRequiredTextView = (TextView) v.findViewById(R.id.amount_required_text_view);
        mEarningsTextView = (TextView) v.findViewById(R.id.earnings_text_view);


        Uri storeUri = mRunnerTask.getOrder().getStore().getPhotoUri();
        if(storeUri!=null)
        {
            Picasso.with(getActivity()).load(storeUri).fit().into(mStoreImageView);
        }

        String customerUri = mRunnerTask.getCustomer().getPictureUrl();

        if(!customerUri.isEmpty()&&!customerUri.equals("null")&&customerUri!=null)
        {
            Picasso.with(getActivity()).load(customerUri).fit().into(mCustomerImageView);
        }


        mNavigateCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customer_location");

                GeoFire customerGeofire = new GeoFire(customerReference);

                customerGeofire.getLocation(mRunnerTask.getCustomer().getUuid(), new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {

                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude+","+location.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        startActivity(mapIntent);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });


        mNavigateStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference storeReference = FirebaseDatabase.getInstance().getReference("stores");

                GeoFire storeGeoFire = new GeoFire(storeReference);

                storeGeoFire.getLocation(mRunnerTask.getOrder().getStore().getUuid(), new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude+","+location.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        startActivity(mapIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });



        mMapButton = (Button) v.findViewById(R.id.view_map_button);
        mViewItemsButton = (Button) v.findViewById(R.id.view_items_button);

        mCustomerNameTextView.setText(mRunnerTask.getCustomer().getName());
        mStoreNameTextView.setText(mRunnerTask.getOrder().getStore().getName());
        String required = String.format("RM %.2f",mRunnerTask.getOrder().getTotalPrice());
        mAmountRequiredTextView.setText(required);

        mViewItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = RunnerShoppingListActivity.newIntent(getActivity(),mRunnerTask.getOrder().getOrderId());
                startActivity(i);
            }
        });


        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = DispatchMapActivity.newIntent(getActivity(),mRunnerTask);

                startActivityForResult(i,REQUEST_DISPATCH);
            }
        });



        return v;
    }


}
