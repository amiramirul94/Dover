package com.android.dishpatch.dover.Controller.Fragment.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.android.dishpatch.dover.R;

/**
 * Created by Lenovo on 6/29/2016.
 */
public class LocationConfirmationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_loaction_confirmation,null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.confirm_loc)
                .setPositiveButton(android.R.string.ok,null)
                .create();
    }
}
