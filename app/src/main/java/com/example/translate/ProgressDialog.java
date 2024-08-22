package com.example.translate;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tashila.pleasewait.PleaseWaitDialog;

public class ProgressDialog {
    PleaseWaitDialog installationDialog = new PleaseWaitDialog();
    PleaseWaitDialog translationDialog = new PleaseWaitDialog();
    PleaseWaitDialog recognitionDialog = new PleaseWaitDialog();


    // Package Installation Dialog
    public @NonNull PleaseWaitDialog getInstallationDialog(Context context) {
        installationDialog = new PleaseWaitDialog(context);
        installationDialog.setEnterTransition(R.anim.fade_in);
        installationDialog.setExitTransition(R.anim.fade_out);
        installationDialog.setHasOptionsMenu(true);
        installationDialog.setCancelable(true);
        installationDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        installationDialog.setTitle("Installing Translation Model");
        installationDialog.setMessage("This is done to speed up the translation, next time you use this. Next time you translate, " +
                "translation will happen in a few seconds, skipping this process");
        installationDialog.show();
        return installationDialog;
    }

    public PleaseWaitDialog dismissInstallationDialog() {
        installationDialog.dismiss();
        return null;
    }

    // Translation Dialog
    public @NonNull PleaseWaitDialog getTranslationDialog(Context context) {
        translationDialog = new PleaseWaitDialog(context);
        translationDialog.setEnterTransition(R.anim.fade_in);
        translationDialog.setExitTransition(R.anim.fade_out);
        translationDialog.setHasOptionsMenu(true);
        translationDialog.setCancelable(true);
        translationDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        translationDialog.setMessage("Translating Text...");
        translationDialog.show();
        return translationDialog;
    }

    public PleaseWaitDialog dismissTranslateDialog() {
        translationDialog.dismiss();
        return null;
    }

    public @NonNull PleaseWaitDialog getRecognizingDialog(Context context) {
        recognitionDialog = new PleaseWaitDialog(context);
        recognitionDialog.setEnterTransition(R.anim.fade_in);
        recognitionDialog.setExitTransition(R.anim.fade_out);
        recognitionDialog.setHasOptionsMenu(true);
        recognitionDialog.setCancelable(true);
        recognitionDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        recognitionDialog.setMessage("Recognizing Text...");
        recognitionDialog.show();
        return recognitionDialog;
    }

    public PleaseWaitDialog dismissRecognizingDialog() {
        recognitionDialog.dismiss();
        return null;
    }
}
