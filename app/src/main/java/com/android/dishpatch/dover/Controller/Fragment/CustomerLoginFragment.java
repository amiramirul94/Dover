package com.android.dishpatch.dover.Controller.Fragment;

import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.CustomerActivity;
import com.android.dishpatch.dover.ui.Activity.UserRegistrationActivity;
import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;

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
public class CustomerLoginFragment extends Fragment {

    private static final String TAG = CustomerLoginFragment.class.toString() ;

    @Email
    private EditText mEmailEditText;


    @Password(min = 6,scheme = Password.Scheme.ANY)
    private EditText mPasswordEditText;

    private Button mLoginButton;
    private TextView mRegisterTextView;


    private Validator validator;
    private String login_url = "http://insvite.com/php/user_login.php";
    private String uuid;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static CustomerLoginFragment newInstance()
    {
        return new CustomerLoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null)
                {
                    uuid = user.getUid();
                }
            }
        };

        validator = new Validator(this);
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                Log.v(TAG,"Validated");
                login();




            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                Log.v(TAG,"Vaidation failed");
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login_user,container, false);

        mEmailEditText = (EditText) v.findViewById(R.id.username_edit_text);
        mPasswordEditText = (EditText) v.findViewById(R.id.password_edit_text);
        mLoginButton = (Button) v.findViewById(R.id.login_button);
        mRegisterTextView = (TextView) v.findViewById(R.id.register_text_view);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Util.isNetworkAvailable(getActivity()))
                {
                    validator.validate();

                }else {
                    Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }

            }
        });

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = UserRegistrationActivity.newIntent(v.getContext());
                startActivity(i);
            }
        });




        return v;

    }

    private void login()
    {
        //receive values
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful())
                {
                    Toast.makeText(getActivity(), "Sign In Failed", Toast.LENGTH_SHORT).show();
                }else{
                    while(uuid==null)
                    {}

                    proceed();
                }
            }
        });
    }


    private void proceed()
    {


            Intent i = CustomerActivity.newIntent(getActivity());
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DoverPreferences.setUserLoggedIn(getActivity(),true);
            DoverPreferences.setPrefUserUUID(getActivity(),uuid);

            startActivity(i);
            getActivity().finish();

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
