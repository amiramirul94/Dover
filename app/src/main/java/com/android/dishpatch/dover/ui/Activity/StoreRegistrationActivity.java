package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.StoreRegistrationFragment;
import com.android.dishpatch.dover.R;

public class StoreRegistrationActivity extends AppCompatActivity {

    public static Intent newIntent(Context context)
    {
        Intent i = new Intent(context,StoreRegistrationActivity.class);

        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_registration);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.restaurant_registration_fragment_container);

        if(fragment==null)
        {
            fragment = StoreRegistrationFragment.newInstance();

            fm.beginTransaction()
                    .add(R.id.restaurant_registration_fragment_container,fragment)
                    .commit();
        }
    }
}
