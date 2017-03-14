package com.android.dishpatch.dover.Controller.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Service.DispatchLocationServices;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.annotation.Email;
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
 * Created by Lenovo on 11/25/2016.
 */

public class CustomerSettingsFragment extends Fragment {

    private static final int REQUEST_PHOTO = 2;
    private static final String TAG = CustomerSettingsFragment.class.getSimpleName();
    private ImageView mProfilePictureImageView;
    private ImageButton mChangeProfilePictureButton;

    @Email
    private EditText mEmailEditText;



    private Button mChangeEmailButton;

    private EditText mChangePasswordEditText;
    private Button mChangePasswordButton;
    private Switch mRunnerSwitch;
    private Button mLogoutButton;

    private TextView mRunnerTextView;

    private Uri mProfilePictureUri;


    private static final String GET_PROFILE_PIC = "http://insvite.com/php/dover/get_customer_profile_picture.php?customer_uuid=";
    private static final String ADD_PROFILE_PIC_URL = "http://insvite.com/php/dover/add_profile_pic_customer.php";



    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;



    private String mEmail;
    String uuid ="";


    public static CustomerSettingsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        CustomerSettingsFragment fragment = new CustomerSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    mEmail = mUser.getEmail();
                    mEmailEditText.setHint(mEmail);
                }

            }
        };

        uuid = DoverPreferences.getPrefUserUUID(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.fragment_customer_settings,container,false);
        mProfilePictureImageView = (ImageView) v.findViewById(R.id.customer_profile_image_view);
        mChangeProfilePictureButton = (ImageButton) v.findViewById(R.id.change_profile_pic_button);
        mEmailEditText = (EditText) v.findViewById(R.id.email_edit_text);
        mChangeEmailButton = (Button) v.findViewById(R.id.change_email_button);
        mChangePasswordEditText = (EditText) v.findViewById(R.id.password_edit_text);
        mChangePasswordButton = (Button) v.findViewById(R.id.change_password_buttton);
        mRunnerSwitch = (Switch) v.findViewById(R.id.runner_switch);
        mLogoutButton = (Button) v.findViewById(R.id.logout_button);
        mRunnerTextView = (TextView) v.findViewById(R.id.runner_text_view);

        mChangeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,REQUEST_PHOTO);
            }
        });

        mChangeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.updateEmail(mEmailEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_SHORT).show();

                        }else{
                            Log.v(TAG,task.getException().toString());
                        }
                    }
                });
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.updatePassword(mChangePasswordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.v(TAG,task.getException().toString());
                        }
                    }
                });
            }
        });

        if(DoverPreferences.getIsDispatchOnline(getActivity()))
        {   mRunnerSwitch.setText("On");
            mRunnerTextView.setText("You are now a runner");
            mRunnerSwitch.setChecked(true);
        }else{
            mRunnerSwitch.setText("Off");
            mRunnerTextView.setText("Be A Runner");
            mRunnerSwitch.setChecked(false);
        }

        mRunnerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mRunnerSwitch.setText("On");
                    mRunnerTextView.setText("You are now a runner");
                    DoverPreferences.setDispatchOnline(getActivity(),true);
                    Intent intent = DispatchLocationServices.newIntent(getActivity());
                    getActivity().startService(intent);

                }else{
                    mRunnerSwitch.setText("Off");
                    mRunnerTextView.setText("Be A Runner");
                    Intent intent = DispatchLocationServices.newIntent(getActivity());
                    DoverPreferences.setDispatchOnline(getActivity(),false);
                    getActivity().stopService(intent);
                }

                Log.v(TAG,isChecked+"");
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = MainActivity.newIntent(getActivity());
                DoverPreferences.setUserLoggedIn(getActivity(),false);
                DoverPreferences.setPrefUserUUID(getActivity(),null);
                startActivity(i);
                getActivity().finish();
            }
        });


        new GetCustomerProfileUrl().execute();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mAuthStateListener!=null)
        {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==Activity.RESULT_OK)
        {
            if(requestCode==REQUEST_PHOTO)
            {
                mProfilePictureUri = data.getData();

                Log.v(TAG,mProfilePictureUri.toString());

                Picasso.with(getActivity()).load(mProfilePictureUri).fit().into(mProfilePictureImageView);
                addImageToFireBase();
            }
        }
    }

    private void addImageToFireBase(){

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageReference = storage.getReferenceFromUrl("gs://dover-fd9a6.appspot.com");

        StorageReference imageRef = storageReference.child("customer/profile-pic/"+uuid+"-profile-pic.jpg");

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
                        .add("customer_uuid",uuid+"")
                        .build();

                Request request =  new Request.Builder().url(ADD_PROFILE_PIC_URL).post(requestBody).build();
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




    private class GetCustomerProfileUrl extends AsyncTask<Void,Void,String>{



        @Override
        protected String doInBackground(Void... params) {

            return getProfilePic();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!s.isEmpty()&&!s.equals("null"))
            {
                Picasso.with(getActivity()).load(s).fit().into(mProfilePictureImageView);
            }

        }

        private String getProfilePic(){

            String url="";
            OkHttpClient client = new OkHttpClient();

            String requestString = GET_PROFILE_PIC+DoverPreferences.getPrefUserUUID(getActivity());

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
