package com.android.dishpatch.dover.ui.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dishpatch.dover.Controller.Fragment.RunnerShoppingListFragment;
import com.android.dishpatch.dover.R;

public class RunnerShoppingListActivity extends AppCompatActivity {

    private static final String ORDER_ID_EXTRA = RunnerShoppingListActivity.class.getSimpleName()+"order_id";
    public static Intent newIntent(Context context, int orderId)
    {
        Intent i = new Intent(context,RunnerShoppingListActivity.class);
        i.putExtra(ORDER_ID_EXTRA,orderId);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_shopping_list);


        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.shopping_list_fragment_container);

        int orderId = getIntent().getIntExtra(ORDER_ID_EXTRA,-1);
        if(fragment==null)
        {
            if(orderId!=-1)
            {
                fragment = RunnerShoppingListFragment.newInstance(orderId);

                fm.beginTransaction().add(R.id.shopping_list_fragment_container,fragment).commit();
            }
        }
    }


}
