package com.example.camerax;

import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;

import com.bumptech.glide.Glide;

import java.io.File;


//saus: https://codelabs.developers.google.com/codelabs/camerax-getting-started/

public class MainActivity2 extends AppCompatActivity {

    private ImageView iamge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        iamge = (ImageView) findViewById(R.id.iamge);

        Bundle bundle = getIntent().getExtras();
        String filePath = bundle.getString("filePath");

        Glide.with(this).load(filePath).into(iamge);
    }
}
