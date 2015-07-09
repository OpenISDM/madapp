package com.mad.openisdm.madnew;
/**
 * Copyright (c) 2014  OpenISDM
 *
 * Project Name:
 *   Mobile Clients for MAD
 *
 * Version:
 *   1.0
 *
 * File Name:
 *   AlertDialogFragment.java
 *
 * Abstract:
 *   AlertDialogFragment.java is the class files in Mobile Clients for MAD project.
 *   AlertDialogFragment will be used as custom dialog message in fragment to user when needed.
 *
 * Authors:
 *   Andre Lukito, routhsauniere@gmail.com
 *
 * License:
 *  GPL 3.0 This file is subject to the terms and conditions defined
 *  in file 'COPYING.txt', which is part of this source code package.
 *
 * Major Revision History:
 *   2014/5/13: complete version 1.0
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {

    protected static double latitude, longitude;
    private AlertDialog.Builder alertDialog;
    private String title, msg;

    /**
     * Function Name:
     * newInstance
     * <p/>
     * Function Description:
     * Create new instance for AlertDialogFragment class and put the variable in argument (Bundle).
     * <p/>
     * Parameters:
     * String title - title of dialog.
     * String msg - message of dialog.
     * int button - mode of button that needed to show in dialog.
     * <p/>
     * Returned Value:
     * Returns AlertDialogFragment Object with set of arguments.
     * <p/>
     * Possible Error Code or Exception:
     * none.
     */
    protected static AlertDialogFragment newInstance(String title, String msg) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        title = getArguments().getString("title");
        msg = getArguments().getString("msg");
        alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);


        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );


        return alertDialog.create();
    }
}
