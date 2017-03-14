package com.android.dishpatch.dover.Controller.SharedPreferences;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Lenovo on 7/26/2016.
 */
public class DoverPreferences {
    private static final String PREF_USER_UUID = "com.android.dover.dover.PREF_USER_UUID";
    private static String PREF_IS_LOGIN ="IS_USER_LOGIN";
    private static String PREF_STORE_LOGIN =  "com.android.dover.dover.IS_STORE_LOGGED_IN";
    private static String PREF_USER_ID = "USER_ID";
    private static String PREF_DISPATCH_ONLINE = "IS_DISPATCH_ONLINE";
    private static String PREF_STORE_UUID = "PRED_STORE_UUID";
    private static String PREF_STORE_OPEN = "PREF_STORE_OPEN";

    public static Boolean getUserLoggedIn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_LOGIN,false);
    }

    public static Boolean getStoreLoggedIn(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_STORE_LOGIN,false);
    }

    public static int getUserId(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_USER_ID,-1);
    }
    public static void setUserLoggedIn(Context context, Boolean isLoggedIn)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_IS_LOGIN,isLoggedIn).apply();
    }

    public static void setPrefStoreLogin(Context context, Boolean isLoggedIn)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_STORE_LOGIN,isLoggedIn).apply();
    }

    public static void setPrefUserId(Context context,int id)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_USER_ID,id).apply();
    }

    public static void setPrefStoreUUID(Context context, String id)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_STORE_UUID,id).apply();

    }

    public static void setPrefUserUUID(Context context, String id)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_USER_UUID,id).apply();
    }

    public static String getPrefUserUUID(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_UUID,null);
    }

    public static String getPrefStoreUUID(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_STORE_UUID,null);

    }
    public static void setDispatchOnline(Context context,boolean isOnline)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_DISPATCH_ONLINE,isOnline).apply();
    }

    public static boolean getIsDispatchOnline(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_DISPATCH_ONLINE,false);
    }

    public static void setPrefStoreOpen(Context context, boolean isOpen)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_STORE_OPEN,isOpen).apply();
    }

    public static boolean getPrefStoreOpen(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_STORE_OPEN,false);
    }
}
