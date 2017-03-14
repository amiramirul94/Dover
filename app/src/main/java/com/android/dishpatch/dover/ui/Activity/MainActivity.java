package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mUserButton;
    private Button mRestaurantButton;
    private Boolean isUserLoggedIn;
    private Boolean isRestaurantLoggedIn;

    public static Intent newIntent(Context context)
    {
        Intent i = new Intent(context,MainActivity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sendUser();


        mUserButton = (Button) findViewById(R.id.user_button);
        mRestaurantButton = (Button) findViewById(R.id.restaurant_button);

        mUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = UserLoginActivity.newIntent(v.getContext());

                startActivity(i);
            }
        });


        mRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = RestaurantLoginActivity.newIntent(v.getContext());
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        sendUser();
    }

    private void sendUser() {



        if(DoverPreferences.getPrefUserUUID(this)!=null){

            Intent intent = CustomerActivity.newIntent(this);
            startActivity(intent);
            Log.v(TAG,DoverPreferences.getPrefUserUUID(this));


        }else if(DoverPreferences.getPrefStoreUUID(this)!=null){
            Log.v(TAG, "Store="+DoverPreferences.getPrefStoreUUID(this));
            Intent intent = StoreActivity.newIntent(this);
            startActivity(intent);

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}