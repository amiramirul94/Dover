package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Controller.Fragment.CustomerLoginFragment;

public class UserLoginActivity extends AppCompatActivity {


    public static Intent newIntent(Context context)
    {
        Intent i = new Intent(context,UserLoginActivity.class);
        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.login_fragment_container);

        if(fragment==null)
        {
            fragment = CustomerLoginFragment.newInstance();

            fm.beginTransaction()
                    .add(R.id.login_fragment_container,fragment)
                    .commit();
        }
    }
}
