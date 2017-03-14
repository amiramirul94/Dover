package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.android.dishpatch.dover.Controller.Fragment.MainDispatchFragment;
import com.android.dishpatch.dover.R;

public class DispatchActivity extends AppCompatActivity {


    private static final String ORDER_ID_EXTRA = "ORDER_ID_EXTRA";
    Integer mOrderId;

    public static Intent newIntent(Context context)
    {
        Intent i = new Intent(context,DispatchActivity.class);

        return i;
    }
    public static Intent newIntent(Context context,int orderId)
    {
        Intent i = new Intent(context,DispatchActivity.class);
        i.putExtra(ORDER_ID_EXTRA,orderId);
        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOrderId = getIntent().getIntExtra(ORDER_ID_EXTRA,-1);
        if(mOrderId!=-1)
        {
            FragmentManager fm = getSupportFragmentManager();

            Fragment fragment = fm.findFragmentById(R.id.main_dispatch_fragment_container);

            if(fragment==null)
            {
                fragment =  MainDispatchFragment.newInstance(mOrderId);

                fm.beginTransaction().add(R.id.main_dispatch_fragment_container,fragment)
                        .commit();
            }
        }





    }

}
