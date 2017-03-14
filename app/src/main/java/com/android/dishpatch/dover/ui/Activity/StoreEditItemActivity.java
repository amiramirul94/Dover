package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.StoreEditItemFragment;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Item;
import com.android.dishpatch.dover.R;

public class StoreEditItemActivity extends AppCompatActivity {

    private static final String MENU_ID = "MENU_ID";
    private static final String TAG = StoreEditItemActivity.class.getSimpleName();

    public static final Intent newIntent(Context context,int id)
    {
        Intent intent = new Intent(context,StoreEditItemActivity.class);
        intent.putExtra(MENU_ID,id);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_edit_menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.fragment_edit_menu_container);

        int id = getIntent().getIntExtra(MENU_ID,0);


        Item item = DoverCentral.get(this).getMenu(id);

        if(fragment==null)

            fragment = StoreEditItemFragment.newInstance(id);
        {
            fm.beginTransaction().add(R.id.fragment_edit_menu_container,fragment).commit();
        }
    }
}
