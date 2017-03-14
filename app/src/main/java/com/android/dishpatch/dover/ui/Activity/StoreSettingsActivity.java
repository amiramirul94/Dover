package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.StoreSettingsFragment;
import com.android.dishpatch.dover.R;

public class StoreSettingsActivity extends AppCompatActivity {


    public static Intent newIntent(Context context)
    {
        Intent i = new Intent(context,StoreSettingsActivity.class);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_settings);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_store_settings_container);

        if(fragment==null)
        {
            fragment = StoreSettingsFragment.newInstance();

            fm.beginTransaction()
                    .add(R.id.fragment_store_settings_container,fragment)
                    .commit();
        }
    }
}
