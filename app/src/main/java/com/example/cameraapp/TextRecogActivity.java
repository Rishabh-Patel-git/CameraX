package com.example.cameraapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextRecogActivity extends AppCompatActivity {
    private Uri imageUri;
    private Button recog;
    private TextView queryText;
    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recog_acitivity);
        Intent i = getIntent();
        Uri imageUri = i.getData();
        int rotationDegree = i.getIntExtra("rotation", 90);
        imageView = findViewById(R.id.crop_image);
        try {

            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Glide.with(this).load(imageUri).into(imageView);


        recog = findViewById(R.id.recog_btn);

        recog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textRecognition(rotationDegree);
            }
        });

    }

    private void textRecognition(int rotationDegree) {

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);
        Task<Text> result = recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {

                               textMaker(visionText);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(TextRecogActivity.this,"failure",Toast.LENGTH_SHORT).show();
                                    }
                                });
    }
    private void textMaker(Text result){
        String resultText = result.getText();
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
        }
        queryText.setText(resultText);

    }

}