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

import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Util;
import com.android.dishpatch.dover.ui.Activity.StoreActivity;
import com.android.dishpatch.dover.ui.Activity.StoreRegistrationActivity;
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

import java.util.List;

/**
 * Created by Lenovo on 6/26/2016.
 */
public class StoreLoginFragment extends Fragment {

    private static final String restaurant_login_url = "http://insvite.com/php/restaurant_login.php";
    private static final String TAG = StoreLoginFragment.class.getSimpleName();

    @Email
    private EditText mRestaurantEmailEditText;

    @Password(min=6, scheme = Password.Scheme.ANY )
    private EditText mPasswordEditText;

    private Button mLoginButton;
    private TextView mRegisterTextView;

    private Validator mValidator;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String storeId;

    public static StoreLoginFragment newInstance()
    {
        return new StoreLoginFragment();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null)
                {
                    storeId = user.getUid();
                }
            }
        };


        mValidator = new Validator(this);
        mValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                login();
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_restaurant_login,container,false);

        mRestaurantEmailEditText = (EditText) v.findViewById(R.id.restaurant_email_edit_text);
        mPasswordEditText = (EditText) v.findViewById(R.id.password_edit_text);
        mLoginButton = (Button) v.findViewById(R.id.login_button);
        mRegisterTextView = (TextView) v.findViewById(R.id.register_text_view);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Util.isNetworkAvailable(getActivity()))
                {
                    mValidator.validate();

                }else{
                    Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }
//                Intent i = StoreActivity.newIntent(v.getContext());
//                startActivity(i);
            }
        });

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = StoreRegistrationActivity.newIntent(v.getContext());
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                getActivity().finish();
            }
        });
        return v;
    }


    private void login()
    {
        //receive values
        String email = mRestaurantEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if(!task.isSuccessful())
                {
                    Toast.makeText(getActivity(), "Sign In Failed", Toast.LENGTH_SHORT).show();
                }else{
                   while (storeId==null)
                   {}

                    Log.v(TAG,storeId);

                    //you may proceed
                    proceed();
                }
            }
        });
    }

    private void proceed(){


            Intent i = StoreActivity.newIntent(getActivity());
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            DoverPreferences.setPrefStoreLogin(getActivity(),true);

            Log.v(TAG,DoverPreferences.getStoreLoggedIn(getActivity()).toString());
            DoverPreferences.setPrefStoreUUID(getActivity(),storeId);
            startActivity(i);
            getActivity().finish();

    }
}
