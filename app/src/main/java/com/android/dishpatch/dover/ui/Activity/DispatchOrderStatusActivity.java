package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.android.dishpatch.dover.Controller.Fragment.DispatchOrderStatusFragment;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.ItemDescription;
import com.android.dishpatch.dover.Model.RunnerTask;
import com.android.dishpatch.dover.R;

public class DispatchOrderStatusActivity extends AppCompatActivity {

    private static final String TAG = "DispatchStatus";
    private static final String EXTRA_DISPATCH_DATA = "com.android.dispatch.dispatch.Runner";
    public static Intent newIntent(Context packageContext, RunnerTask runnerTask)
    {
        Intent intent = new Intent(packageContext,DispatchOrderStatusActivity.class);
        intent.putExtra(EXTRA_DISPATCH_DATA, runnerTask);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch_order_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RunnerTask runnerTask;

        runnerTask = (RunnerTask) DoverCentral.get(this).getValue("ORDER_DATA");
        toolbar.setTitle(runnerTask.getCustomer().getName());
        setSupportActionBar(toolbar);

        //Log.d(TAG,getIntent().getParcelableExtra(EXTRA_DISPATCH_DATA).toString());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.fragment_dispatch_order_status_container);

        if(fragment==null)
        {


            fragment = DispatchOrderStatusFragment.newInstance(runnerTask);
            fm.beginTransaction()
                    .add(R.id.fragment_dispatch_order_status_container,fragment)
                    .commit();
        }
    }

}
