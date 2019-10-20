package com.ticonsys.cameraimagecropdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

public class MainActivity extends AppCompatActivity {

    private ImageView ivImage;

    private Uri mResultUri;


    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ivImage = findViewById(R.id.ivImage);

        findViewById(R.id.btnPickupCamera).setOnClickListener((view) ->{
            cameraButtonClick();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent();
            } else {
                Toast.makeText(
                        this,
                        "Cancelling, required permissions are not granted",
                        Toast.LENGTH_LONG
                ).show();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                );
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mResultUri = result.getUri();
                ivImage.setImageURI(mResultUri);
                Log.d(TAG, "Result URI: $mResultUri");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "ERROR: "+error.getMessage());
            }
        }

    }

    private void cameraButtonClick(){
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                );
            }
        } else {
            startCameraIntent();
        }
    }

    private void startCameraIntent(){
        Intent cameraIntent = CropImage.getCameraIntent(this, null);
        startActivityForResult(cameraIntent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    private void startCropImageActivity(Uri imageUri)
    {
        CropImage.activity(imageUri)
                .start(this);
    }
}
