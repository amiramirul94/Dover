package com.android.dishpatch.dover.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.ui.Activity.DispatchActivity;
import com.android.dishpatch.dover.ui.Activity.MainActivity;
import com.android.dishpatch.dover.ui.Activity.OrderInfoActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lenovo on 8/18/2016.
 */
public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        String type = remoteMessage.getData().get("type");

        Log.v(TAG,type);
        String message = remoteMessage.getData().get("message");
        if(type.equals("dispatch"))
        {
            String data = remoteMessage.getData().get("order_object");
            showDispatchNotification(data);
        }else if(type.equals("order_update")){
            String data = remoteMessage.getData().get("order_object");

           showOrderUpdateNotification(data,message);
        }

        //showNotification(remoteMessage.getData().get("message"));
    }

    private void showDispatchNotification(String data) {
        int order_id=-1;

        try {
            JSONObject restaurantObject = new JSONObject(data);
            order_id = Integer.parseInt(restaurantObject.getString("order_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG,data);
        Intent i = DispatchActivity.newIntent(this,order_id);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Check if user is logged in
        //Check whether the notification is intended for restaurant or customer

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("New Delivery")
                .setContentText("You have been dispatched")
                .setSmallIcon(R.mipmap.icon_dover)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
        vibratePhone(3000);

    }

    private void showOrderUpdateNotification(String data, String message)
    {
        int order_id=-1;

        try {
            JSONObject restaurantObject = new JSONObject(data);
            order_id = Integer.parseInt(restaurantObject.getString("order_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        Intent i = OrderInfoActivity.newIntent(this,order_id);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Check if user is logged in
        //Check whether the notification is intended for restaurant or customer

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Order Update")
                .setContentText(message)
                .setSmallIcon(R.mipmap.icon_dover)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
        vibratePhone(2000);
    }

    private void showNotification(String message) {

        Intent i = new Intent(this,MainActivity.class);


        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Check if user is logged in
        //Check whether the notification is intended for restaurant or customer

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("FCM Test")
                .setContentText(message)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        manager.notify(0,builder.build());
    }

    private void vibratePhone(long ms)
    {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(ms);
    }

}
