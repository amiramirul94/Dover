package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.dishpatch.dover.Controller.Fragment.SelectItemFragment;
import com.android.dishpatch.dover.Model.Store;
import com.android.dishpatch.dover.R;

public class SelectItemActivity extends AppCompatActivity {

    public static final String EXTRA_STORE = "com.android.dishpatch.dover.store_extra";
    public static final String RESTAURANT_SAVED = "RESTAURANT_STATE";
    public static final String TAG = "SelectItemActivity";
    private Store mStore;

    public static Intent newIntent(Context context,Store store)
    {
        Intent intent = new Intent(context,SelectItemActivity.class);
        intent.putExtra(EXTRA_STORE, store);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        mStore =  getIntent().getParcelableExtra(EXTRA_STORE);

        Log.v(TAG,mStore.getName());

        toolbar.setTitle(mStore.getName());
        setSupportActionBar(toolbar);




        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.select_food_fragment_container);

        if(fragment==null)
        {

            fragment = SelectItemFragment.newInstance(mStore);

            fm.beginTransaction()
                    .add(R.id.select_food_fragment_container,fragment)
                    .commit();
        }



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }





}
