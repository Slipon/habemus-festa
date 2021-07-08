package com.example.habemusfesta.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.habemusfesta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Utils {

    public static final String TAG_QR_CODE = "QR_CODE: ";
    public static final String TAG_CONNECTION = "CONNECTION: ";
    public static boolean isConnected = true;
    public static void openQRCodePopup(FirebaseAuth mAuth, Dialog mDialog, int width){
        Button closeQrCodeBtn;
        ImageView qrCodeImg;
        mDialog.setContentView(R.layout.qr_code_popup_window);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        closeQrCodeBtn = mDialog.findViewById(R.id.closeQrCodeBtn);
        closeQrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        qrCodeImg = mDialog.findViewById(R.id.qrCodeImg);
        generateQRCode(qrCodeImg, mAuth.getUid(), width);
        mDialog.show();
    }

    public static void showTransactionStatus(Context context, String status, Dialog mDialog){
        ImageView transactionStatusImg;
        ImageView transactionStatusTxt;
        Button closeTransactionStatusBtn;

        mDialog.setContentView(R.layout.transaction_status_popup_window);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        transactionStatusImg = mDialog.findViewById(R.id.transactionImg);
        transactionStatusTxt = mDialog.findViewById(R.id.transactionStatus);

        Drawable resImg;
        Drawable resTxt;

        if(status.equals("COMPLETE")){
            resTxt = ContextCompat.getDrawable(context, R.drawable.title_purchase_completed);
            resImg = ContextCompat.getDrawable(context, R.drawable.ic_purchase_success);
        }else{
            resTxt = ContextCompat.getDrawable(context, R.drawable.title_purchase_failed);
            resImg = ContextCompat.getDrawable(context, R.drawable.ic_purchase_failed);
        }

        transactionStatusImg.setBackground(null);
        transactionStatusImg.setImageDrawable(resImg);

        transactionStatusImg.setBackground(null);
        transactionStatusTxt.setImageDrawable(resTxt);

        closeTransactionStatusBtn = mDialog.findViewById(R.id.closeTransactionStatusBtn);
        closeTransactionStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    public static boolean checkInternetConnection(Context context){
        if(isNetworkConnected(context)){
            return internetIsConnected();
        }
        return false;
    }


    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private static boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    private static void generateQRCode(ImageView qrCodeImage, String uid, int width){

        QRGEncoder qrgEncoder = new QRGEncoder(uid, null, QRGContents.Type.TEXT, width);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrCodeImage.setImageBitmap(bitmap);
        } catch (IllegalArgumentException e) {
            Log.v(TAG_QR_CODE, e.toString());
        }
    }

}
