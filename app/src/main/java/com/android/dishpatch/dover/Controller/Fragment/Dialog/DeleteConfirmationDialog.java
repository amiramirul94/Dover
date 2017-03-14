package com.android.dishpatch.dover.Controller.Fragment.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.android.dishpatch.dover.R;

/**
 * Created by Lenovo on 8/14/2016.
 */
public class DeleteConfirmationDialog extends DialogFragment {

    public static final String EXTRA_CONFIRMATION = "com.android.dishpatch.dishpatch.boolean";
    public static  DeleteConfirmationDialog newInstance()
    {
        return new DeleteConfirmationDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete_confirmation,null);

        return new AlertDialog.Builder(getActivity()).setView(v).setTitle(R.string.confirmation_delete_title)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK,true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK,false);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode,Boolean isConfirmed)
    {
        if(getTargetFragment()==null)
        {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONFIRMATION,isConfirmed);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
