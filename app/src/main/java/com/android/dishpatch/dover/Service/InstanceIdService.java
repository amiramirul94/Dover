package com.android.dishpatch.dover.Service;

import android.util.Log;

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 8/18/2016.
 */
public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String UPDATE_STORE_TOKEN_URL = "http://insvite.com/php/dover/add_store_token.php";
    private static final String UPDATE_CUSTOMER_TOKEN_URL = "http://insvite.com/php/dover/add_customer_token.php";

    private static final String TAG = InstanceIdService.class.getSimpleName() ;


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.v(TAG,token);
        if(DoverPreferences.getStoreLoggedIn(this)&&DoverPreferences.getPrefStoreUUID(this)!=null)
        {
            registerStoreToken(token);
        }else if(DoverPreferences.getUserLoggedIn(this)&&DoverPreferences.getPrefUserUUID(this)!=null){
            registerCustomerToken(token);
        }

    }

    private void registerStoreToken(String token) {

        String uuid = DoverPreferences.getPrefStoreUUID(this);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("store_uuid",uuid+"")
                .add("token",token)
                .build();

        Request request = new Request.Builder()
                .url(UPDATE_STORE_TOKEN_URL)
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void registerCustomerToken(String token)
    {
        String uuid = DoverPreferences.getPrefUserUUID(this);

        OkHttpClient client = new OkHttpClient();



        RequestBody requestBody = new FormBody.Builder().add("customer_uuid",uuid+"").add("token",token).build();

        Request request = new Request.Builder().post(requestBody).url(UPDATE_CUSTOMER_TOKEN_URL).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.isSuccessful())
                {
                    Log.v(TAG,"token updated");
                }
            }
        });

    }
}
