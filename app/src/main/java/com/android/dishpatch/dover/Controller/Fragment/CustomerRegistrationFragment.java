package com.android.dishpatch.dover.Controller.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.UserLoginActivity;
import com.android.dishpatch.dover.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 6/26/2016.
 */
public class CustomerRegistrationFragment extends Fragment {


    private static final String TAG = CustomerRegistrationFragment.class.getSimpleName();
    private static final int CHOOSE_PROFILE_PIC = 1;

    @NotEmpty
    private EditText mUsernameEditText;

    @Password (min = 6,scheme = Password.Scheme.ANY)
    private EditText mPasswordEditText;

    @Email
    private EditText mEmailEditText;

    private Button mRegisterButton;
    private ImageView mProfilePicImageView;
    private ImageButton mAddProfilePicImageButton;
    private Validator mValidator;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private String register_url = "http://insvite.com/php/dover/register_customer.php";
    private static final String ADD_PROFILE_PIC_URL = "http://insvite.com/php/dover/add_profile_pic_customer.php";
    private Uri mProfilePicImageUri;
    private String uuid;


    public static CustomerRegistrationFragment newInstance()
    {
        return new CustomerRegistrationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null) {

                    uuid = user.getUid();
                }
            }
        };

        mValidator = new Validator(this);
        mValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                registerToFirebase();
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_registaration,container,false);

        mUsernameEditText = (EditText) v.findViewById(R.id.username_edit_text);
        mPasswordEditText = (EditText) v.findViewById(R.id.password_edit_text);
        mEmailEditText = (EditText) v.findViewById(R.id.email_edit_text);
        mRegisterButton = (Button) v.findViewById(R.id.register_button);
        mProfilePicImageView = (ImageView) v.findViewById(R.id.profile_picture_image_view);
        mAddProfilePicImageButton = (ImageButton) v.findViewById(R.id.add_profile_pic_image_button);

        mAddProfilePicImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PROFILE_PIC);
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Util.isNetworkAvailable(getActivity()))
                {
                    mValidator.validate();

                }else{
                    Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }
            }
        });


        return v;
    }


    private void registerToFirebase()
    {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        mFirebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(!task.isSuccessful())
                {
                    Log.v(TAG,"Registration failed");
                }else{

                    while(uuid==null){}

                    registerToDatabase();

                }
            }
        });
    }

    private void registerToDatabase()
    {
        OkHttpClient client = new OkHttpClient();

        String username = mUsernameEditText.getText().toString();



        RequestBody requestBody = new FormBody.Builder()
                .add("customer_uuid",uuid)
                .add("name",username)
                .build();
        Request request =  new Request.Builder().url(register_url).post(requestBody).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if(response.isSuccessful())
                    {
                        Log.v(TAG,"OK");
                        String responseString = response.body().string();
                        Log.v(TAG,responseString);

                        JSONObject jsonBody = new JSONObject(responseString);
//                        uuid = jsonBody.getString("id");

                        if(mProfilePicImageUri!=null)
                        {
                            addImageToFirebase();
                        }
                        Intent intent = UserLoginActivity.newIntent(getActivity());
                        startActivity(intent);
                    }else{
                        Log.v(TAG,"Failed");
                    }
                } catch (JSONException e) {
                    Log.e(TAG,"Exception caught ",e);

                }
            }
        });




    }

    private void addImageToFirebase()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageReference = storage.getReferenceFromUrl("gs://dover-fd9a6.appspot.com");

        StorageReference imageRef = storageReference.child("customer/profile-pic/"+uuid+"-profile-pic.jpg");

        UploadTask uploadTask = imageRef.putFile(mProfilePicImageUri);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==CHOOSE_PROFILE_PIC)
            {
                mProfilePicImageUri = data.getData();

                Log.v(TAG,mProfilePicImageUri.toString());
                Picasso.with(getActivity()).load(mProfilePicImageUri).fit().centerCrop().into(mProfilePicImageView);
            }
        }
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
}
