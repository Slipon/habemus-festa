package com.example.habemusfesta.utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.habemusfesta.R;
import com.google.zxing.Result;

public class QRScanner extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView codeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        codeScannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this,codeScannerView);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!result.equals("")) {
                            Intent givenIntent = getIntent();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("uid", result.toString());
                            returnIntent.putExtra("product", givenIntent.getStringExtra("product"));
                            returnIntent.putExtra("points", givenIntent.getStringExtra("points"));
                            setResult(givenIntent.getIntExtra("code", 0), returnIntent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        codeScanner.startPreview(); //when the activity starts, the camera starts capturing the frames

    }
}
