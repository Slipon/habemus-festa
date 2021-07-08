package com.example.habemusfesta.products;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.habemusfesta.R;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageActivity extends AppCompatActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        photoView = findViewById(R.id.photo_view);
        String eventImageUrl = getIntent().getStringExtra("image_url");
        if(eventImageUrl != null) {
            Glide.with(ImageActivity.this)
                    .load(eventImageUrl)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(photoView);
        }else{
            Toast.makeText(ImageActivity.this, R.string.event_page_url_warning,
                    Toast.LENGTH_SHORT).show();
        }

    }
}