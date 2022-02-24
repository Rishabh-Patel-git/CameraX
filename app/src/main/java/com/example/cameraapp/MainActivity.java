package com.example.cameraapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS =
            new String[]{"android.permission.CAMERA"};
    private ProcessCameraProvider cameraProvider;
    private ImageButton mCaptureBtn;
    private PreviewView mPreview;
    private ImageCapture mImageCapture;
    private ImageAnalysis imageAnalysis;
    private int rotationDegrees;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCaptureBtn = findViewById(R.id.capture_button);
        mPreview = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        else{
            cameraProviderFuture.addListener(() -> {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    startCamera(cameraProvider);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, getExecutor());
            mCaptureBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    capturePhoto();
                }
            });
        }

    }

    private void capturePhoto() {
        mImageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                Bitmap imageBitmap = imageProxyToBitmap(image);
                Uri imageUri = getImageUri(MainActivity.this, imageBitmap);
                Intent i = new Intent(MainActivity.this, TextRecogActivity.class);
                i.setData(imageUri);
                i.putExtra("rotation", rotationDegrees);
                startActivity(i);
                //CropImage.activity(imageUri).start(MainActivity.this);


            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        });
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mPreview.getSurfaceProvider());
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

            }
        });
        mImageCapture = new ImageCapture.Builder().
                setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, mImageCapture,imageAnalysis);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();
//                Intent i = new Intent(MainActivity.this, TextRecogActivity.class);
//                i.setData(resultUri);
//                i.putExtra("rotation", rotationDegrees);
//                startActivity(i);
//
//
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }
//    }


}