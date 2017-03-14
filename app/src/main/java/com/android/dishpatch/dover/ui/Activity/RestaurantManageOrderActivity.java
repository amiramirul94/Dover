package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Controller.Fragment.RestaurantManageOrderFragment;
import com.android.dishpatch.dover.Util;

public class RestaurantManageOrderActivity extends AppCompatActivity {

    private static final String TAG = "ManageOrderActivity";

    public static Intent newIntent(Context packageContext)
    {
        Intent intent = new Intent(packageContext,RestaurantManageOrderActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_manage_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Manage ItemDescription");
        setSupportActionBar(toolbar);



        ItemDescription itemDescription = (ItemDescription) DoverCentral.get(this).getValue(Util.RESTAURANT_ORDER);

        Log.d(TAG, itemDescription.toString());

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.container_fragment_manage_order);

        if(fragment==null)
        {
            fragment = RestaurantManageOrderFragment.newInstance(itemDescription);

            fm.beginTransaction()
                    .add(R.id.container_fragment_manage_order,fragment)
                    .commit();

        }



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
