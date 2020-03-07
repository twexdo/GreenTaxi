package com.adrian.sofergt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class PermissionsListenerClass implements MultiplePermissionsListener {

    final MainActivity mainActivity;
    PermissionToken _token;

    public PermissionsListenerClass(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (!report.areAllPermissionsGranted()) {
            //mainActivity.finish();
            //android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            //configureaza butonul pentru a putea folosi mapa

            //continua activitatea
            mainActivity.continueActivity();
        }

    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        _token = token;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity).setTitle("Am nevoie de aceasta permisiune -_- ").
                setMessage("Pentru a putea folosi aplicatia accepta permisiunile...")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _token.continuePermissionRequest();
                        dialog.dismiss();

                    }
                })
                .setNegativeButton("Refuz", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _token.cancelPermissionRequest();
                        dialog.dismiss();
                        mainActivity.finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        _token.cancelPermissionRequest();
                        dialog.dismiss();
                        mainActivity.finish();
                    }
                });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }
}
