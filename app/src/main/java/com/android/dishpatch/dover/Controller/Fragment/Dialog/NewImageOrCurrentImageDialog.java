package com.android.dishpatch.dover.Controller.Fragment.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.android.dishpatch.dover.R;

/**
 * Created by Lenovo on 12/1/2016.
 */

public class NewImageOrCurrentImageDialog extends DialogFragment {

    public static final String EXTRA_NEW_OR_EXISTING = NewImageOrCurrentImageDialog.class.getSimpleName()+"EXTRA_EXISTING_OR_NEW";


    public static NewImageOrCurrentImageDialog newInstance() {

        NewImageOrCurrentImageDialog fragment = new NewImageOrCurrentImageDialog();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_or_current_image,null);

        return new android.support.v7.app.AlertDialog.Builder(getActivity()).setView(v).setTitle(R.string.choose_image)
                .setPositiveButton("New", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK,true);
                    }
                })
                .setNegativeButton("Existing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK,false);
                    }
                })
                .create();

    }


    private void sendResult(int resultCode,Boolean isNew)
    {
        if(getTargetFragment()==null)
        {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NEW_OR_EXISTING,isNew);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
