package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Service.DispatchLocationServices;
import com.android.dishpatch.dover.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 3/8/2016.
 */
public class GoOnlineRunnerFragment extends Fragment {


    private static final int LOCATION_PERMISSION = 1;
    private static final String TAG = GoOnlineRunnerFragment.class.getSimpleName();
    private static final String ADD_RUNNER_URL = "http://insvite.com/php/dover/add_runner.php";


    public static GoOnlineRunnerFragment newInstance()
    {
        return new GoOnlineRunnerFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dispatch,container,false);

        return v;
    }



    private void sendDispatcherToServer()
    {

        String uuid = DoverPreferences.getPrefUserUUID(getActivity());
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("customer_uuid",uuid+"").build();

        Request request = new Request.Builder().url(ADD_RUNNER_URL).post(requestBody).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               Log.v(TAG,"SUCCESFULLY ADDED");
            }
        });



    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }





}
