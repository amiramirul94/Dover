package com.android.dishpatch.dover.Controller.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.Fragment.Dialog.LocationConfirmationDialog;
import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.MainActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 11/28/2016.
 */

public class StoreSettingsFragment extends Fragment {


    private static final String GET_PROFILE_PIC = "http://insvite.com/php/dover/get_store_profile_picture.php?store_uuid=";
    private static final int REQUEST_PHOTO =  3;
    private static final String ADD_TORE_PIC_URL = "http://insvite.com/php/dover/store_image.php";
    private static final String TAG = StoreSettingsFragment.class.getSimpleName() ;
    private static final String UPDATE_STORE_NAME = "http://insvite.com/php/dover/update_store_name.php";
    private static final int LOCATION_PERMISSION = 1;
    private static final String LOCATION_CONFIRMATION = "LocationConfirmation" ;
    private ImageView mStoreProfileImageView;
    private ImageButton mUpdateProfilePictureButton;
    private EditText mChangeNameEditText;
    private Button mChangeNameButton;
    private Button mChangeLocationButton;
    private TextView mStoreStatusTextView;
    private Switch mStoreStatusSwitch;
    private Button mLogoutButton;
    private Uri mProfilePictureUri;


    private GoogleApiClient mClient;
    private Location mLocation;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;



    public static StoreSettingsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        StoreSettingsFragment fragment = new StoreSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.isGpsIsEnabled(getActivity());

        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }


        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mUser = firebaseAuth.getCurrentUser();

                if (mUser!=null)
                {
                    mChangeNameEditText.setHint(mUser.getDisplayName());
                }

            }
        };


        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        //findLocation();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);


                            } else if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                            }
                        } else {

                        }


                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }


                }).build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_store_settings,container,false);
        mStoreProfileImageView = (ImageView) v.findViewById(R.id.store_profile_image_view);
        mUpdateProfilePictureButton = (ImageButton) v.findViewById(R.id.store_change_profile_pic_button);
        mChangeNameEditText = (EditText) v.findViewById(R.id.store_name_edit_text);
        mChangeNameButton = (Button) v.findViewById(R.id.store_change_name_button);
        mChangeLocationButton = (Button) v.findViewById(R.id.update_location_button);
        mStoreStatusTextView = (TextView) v.findViewById(R.id.store_open_text_view);
        mStoreStatusSwitch = (Switch) v.findViewById(R.id.store_open_switch);
        mLogoutButton = (Button) v.findViewById(R.id.store_logout_button);



        mUpdateProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,REQUEST_PHOTO);
            }
        });

        mChangeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mChangeNameEditText.getText()!=null)
                {
                    updateName();

                }
            }
        });

        mChangeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        mStoreStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSoreStatus(isChecked);
            }
        });

        updateSwitch(DoverPreferences.getPrefStoreOpen(getActivity()));

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference storeReference = FirebaseDatabase.getInstance().getReference("store_status");
                storeReference.child(DoverPreferences.getPrefStoreUUID(getActivity())).child("status").setValue(false+"");
                FirebaseAuth.getInstance().signOut();
                Intent i = MainActivity.newIntent(getActivity());
                DoverPreferences.setPrefStoreLogin(getActivity(),false);
                DoverPreferences.setPrefStoreUUID(getActivity(),null);
                DoverPreferences.setPrefStoreOpen(getActivity(),false);


                startActivity(i);
                getActivity().finish();
            }
        });


        new GetStoreProfilePictureTask().execute();



        return v;

    }

    private void updateSoreStatus(boolean isChecked) {

        DatabaseReference storeReference = FirebaseDatabase.getInstance().getReference("store_status");
        storeReference.child(DoverPreferences.getPrefStoreUUID(getActivity())).child("status").setValue(isChecked);

        DoverPreferences.setPrefStoreLogin(getActivity(),isChecked);
        updateSwitch(isChecked);




    }

    public void updateSwitch(boolean isChecked)
    {
        if(isChecked)
        {
            mStoreStatusSwitch.setText("Open");
            mStoreStatusTextView.setText("Store Open");
            DoverPreferences.setPrefStoreOpen(getActivity(),isChecked);
        }else{
            mStoreStatusSwitch.setText("Closed");
            mStoreStatusTextView.setText("Store Closed");
            DoverPreferences.setPrefStoreOpen(getActivity(),isChecked);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode==REQUEST_PHOTO)
            {
                mProfilePictureUri = data.getData();

                Picasso.with(getActivity()).load(mProfilePictureUri).fit().into(mStoreProfileImageView);
                addImageToFireBase();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }


    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
        if(mAuthStateListener!=null)
        {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case LOCATION_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Permission granted");

                }
                return;

        }

    }

    private void addImageToFireBase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageReference = storage.getReferenceFromUrl("gs://dover-fd9a6.appspot.com");

        StorageReference imageRef = storageReference.child("stores/profile-pic/"+DoverPreferences.getPrefStoreUUID(getActivity())+"-profile-pic.jpg");

        UploadTask uploadTask = imageRef.putFile(mProfilePictureUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("image_url",downloadUrl.toString())
                        .add("store_uuid",DoverPreferences.getPrefStoreUUID(getActivity())+"")
                        .build();

                Request request =  new Request.Builder().url(ADD_TORE_PIC_URL).post(requestBody).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {


                        if(response.isSuccessful())
                        {
                            Log.v(TAG,response.body().string());

                        }
                    }
                });

            }
        });

    }

    private void updateName()
    {
        String name = mChangeNameEditText.getText().toString();

        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        mUser.updateProfile(changeRequest);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("store_name",name)
                .add("store_uuid",DoverPreferences.getPrefStoreUUID(getActivity())+"")
                .build();

        Request request =  new Request.Builder().url(UPDATE_STORE_NAME).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                if(response.isSuccessful())
                {
                    Log.v(TAG,response.body().string());

                }
            }
        });

        Toast.makeText(getActivity(), "Store Name Updated", Toast.LENGTH_SHORT).show();
    }

    private void getLocation()
    {

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG,"location:" + location);
                    mLocation = location;
                    updateLocation();
                }


            });
        }catch(SecurityException e){

            Toast.makeText(getActivity(), "Location permission missing", Toast.LENGTH_SHORT).show();
        }


    }

    private void updateLocation() {
        DatabaseReference storeReference = FirebaseDatabase.getInstance().getReference("stores");
        GeoFire geofire = new GeoFire(storeReference);

        String uuid = DoverPreferences.getPrefStoreUUID(getActivity());

        geofire.setLocation(uuid,new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()));
        Toast.makeText(getActivity(), "Location Updated", Toast.LENGTH_SHORT).show();
    }


    private class GetStoreProfilePictureTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            return getProfilePic();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!=null&&!s.isEmpty()&&!s.equals("null"))
            {
                Picasso.with(getActivity()).load(s).fit().into(mStoreProfileImageView);

            }
        }

        private String getProfilePic(){

            String url="";
            OkHttpClient client = new OkHttpClient();

            String requestString = GET_PROFILE_PIC+ DoverPreferences.getPrefStoreUUID(getActivity());

            Request request = new Request.Builder().url(requestString).build();

            Call call = client.newCall(request);


            try {
                Response response = call.execute();
                if(response.isSuccessful())
                {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    url = parseResponse(jsonObject);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return url;
        }

        private String parseResponse(JSONObject jsonObject) throws JSONException{

            String url = jsonObject.getString("profile_picture");

            return url;

        }
    }
}
