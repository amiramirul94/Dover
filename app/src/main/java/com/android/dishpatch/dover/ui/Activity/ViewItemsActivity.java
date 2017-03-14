package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.ViewItemsFragment;
import com.android.dishpatch.dover.R;

public class ViewItemsActivity extends AppCompatActivity {

    private static final String EXTRA_GET_ORDER_ID =ViewItemsActivity.class.getSimpleName()+"EXTRA_ORDER_ID";
    private int orderId;

    public static Intent newIntent(Context context,int order_id){

        Intent i = new Intent(context,ViewItemsActivity.class);
        i.putExtra(EXTRA_GET_ORDER_ID,order_id);

        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items);

        orderId = getIntent().getIntExtra(EXTRA_GET_ORDER_ID,-1);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.view_items_fragment_container);

        if(fragment==null)
        {
            fragment = ViewItemsFragment.newInstance(orderId);

            fm .beginTransaction().add(R.id.view_items_fragment_container,fragment).commit();
        }
    }
}
