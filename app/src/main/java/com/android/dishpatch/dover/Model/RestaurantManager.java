package com.android.dishpatch.dover.Model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 2/16/2016.
 */
public class RestaurantManager {

    private static RestaurantManager sRestaurantManager;
    private Context mContext;
    private List<Store> mStores;

    public static RestaurantManager get(Context context)
    {
        if(sRestaurantManager==null)
        {
            sRestaurantManager= new RestaurantManager(context);
        }

        return sRestaurantManager;
    }

    private RestaurantManager(Context context)
    {
        mContext = context;
        mStores = new ArrayList<>();

        for(int i=0; i<20; i++)
        {
            Store store = new Store();
            store.setName("Store # "+i);
            store.setDistance(100*(i+1));
            store.setRating(4);
            mStores.add(store);
        }

    }

    public List<Store> getStores() {
        return mStores;
    }


}
