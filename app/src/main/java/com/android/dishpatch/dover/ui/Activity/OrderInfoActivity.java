package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.OrderInfoFragment;
import com.android.dishpatch.dover.Model.Order;
import com.android.dishpatch.dover.R;

public class OrderInfoActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = OrderInfoActivity.class.getSimpleName()+"EXTRA_ORDER_ID";
    public static final String EXTRA_TRACK_ORDER = OrderInfoActivity.class.getSimpleName()+"EXTRA_TRACK_ORDER";
    private Order mOrder;

    public static Intent newIntent(Context context,int orderId)
    {
        Intent intent = new Intent(context,OrderInfoActivity.class);
        intent.putExtra(EXTRA_ORDER_ID,orderId);

        return intent;
    }

    public static Intent newIntent(Context context, Order order)
    {
        Intent intent = new Intent(context,OrderInfoActivity.class);
        intent.putExtra(EXTRA_TRACK_ORDER, order);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID,-1);
        mOrder = getIntent().getParcelableExtra(EXTRA_TRACK_ORDER);


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_order_info_container);


        if(fragment==null){
            // if sent order id;
            if(orderId!=1)
            {

                fragment = OrderInfoFragment.newInstance(orderId);



            }else if(mOrder !=null){

                fragment = OrderInfoFragment.newInstance(mOrder);

            }

            fm.beginTransaction().add(R.id.fragment_order_info_container,fragment).commit();
        }





    }
}
