package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.android.dishpatch.dover.Controller.Fragment.DispatchListFragment;
import com.android.dishpatch.dover.R;

public class DispatchListActivity extends AppCompatActivity {

    public static Intent newIntent(Context context)
    {
        Intent intent = new Intent(context,DispatchListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.dispatch_list_fragment_container);
        if(fragment==null)
        {
            fragment = DispatchListFragment.newInstance();

            fm.beginTransaction().add(R.id.dispatch_list_fragment_container,fragment)
                    .commit();
        }


    }

}
