package com.bookstore.palvindersingh;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;

import java.io.ByteArrayInputStream;
import java.util.List;


public class scanBookActivity extends AppCompatActivity {

    //initialise attributes
    private int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_book);

        //initialise toolbar
        Toolbar myChildToolbar = findViewById(R.id.my_child_toolbar);
        myChildToolbar.setTitle("scan a book");
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        //initialise ui components
        final TextView isbnData = findViewById(R.id.isbn);
        final networkFAB scan = findViewById(R.id.scan);
        FloatingActionButton flash = findViewById(R.id.flash);
        final CameraView camera = findViewById(R.id.camera);
        //initialise camera
        camera.setLifecycleOwner(this);
        //add camera picture taken listener
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                //convert image to a bitmap
                Bitmap bitmap = convertToBitmap(picture);
                //instantiate options class
                FirebaseVisionBarcodeDetectorOptions options =
                        new FirebaseVisionBarcodeDetectorOptions.Builder()
                                .setBarcodeFormats(
                                        FirebaseVisionBarcode.FORMAT_EAN_13,
                                        FirebaseVisionBarcode.FORMAT_EAN_8)
                                .build();
                //convert bitmap to firebase image
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                //instantiate detector class
                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
                //check for barcodes in the image
                Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                //check if there is more than one
                                if (barcodes.size() > 1) {
                                    //prompt the user that there are too many in the frame
                                    Toast.makeText(getApplicationContext(), "too many barcodes", Toast.LENGTH_SHORT).show();
                                } else {
                                    //if there is only one barcode
                                    if (barcodes.size() == 1) {
                                        //set attempts to zero
                                        attempts = 0;
                                        //extract isbn value
                                        String rawValue = String.valueOf(barcodes.get(0).getRawValue());
                                        isbnData.setText(rawValue);
                                        //prompt the user a barcode is found
                                        Toast.makeText(getApplicationContext(), "barcode found", Toast.LENGTH_SHORT).show();
                                        //go to add book activity
                                        Intent intent = new Intent(scanBookActivity.this, addBookActivity.class);
                                        //pass isbn value into intent
                                        intent.putExtra("isbn", rawValue);
                                        startActivity(intent);
                                    } else {
                                        //increment attempts
                                        attempts += 1;
                                        //prompt the user no barcode is found
                                        Toast.makeText(getApplicationContext(), "no barcode found", Toast.LENGTH_SHORT).show();
                                        if (attempts > 2) {
                                            //create alert dialog for manual isbn input
                                            AlertDialog.Builder builder = new AlertDialog.Builder(scanBookActivity.this);
                                            final EditText input = new EditText(scanBookActivity.this);
                                            input.setHint("enter an isbn");
                                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                                            builder.setView(input);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String rawValue = input.getText().toString();
                                                    Intent intent = new Intent(scanBookActivity.this, addBookActivity.class);
                                                    intent.putExtra("isbn", rawValue);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    attempts = 1;
                                                    dialog.cancel();
                                                }
                                            });
                                            builder.show();
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "eRrOr OcCuRrEd", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //on click listener to take a picture
        View.OnClickListener clickEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.capturePicture();
            }
        };
        scan.setClickEvent(clickEvent);

        //on click listener to turn flash on and off
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera.getFlash() == Flash.TORCH) {
                    camera.setFlash(Flash.OFF);
                } else {
                    camera.setFlash(Flash.TORCH);
                }
            }
        });

        //on click listener
        FloatingActionButton manual = findViewById(R.id.manual);
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to add book manual activity
                Intent intent = new Intent(scanBookActivity.this, addBookManualActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rawValue = "1782944141";
                Intent intent = new Intent(scanBookActivity.this, addBookActivity.class);
                intent.putExtra("isbn", rawValue);
                startActivity(intent);
            }
        });

    }

    //method to convert byte array into a bitmap
    private Bitmap convertToBitmap(byte[] picture) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(picture);
        return BitmapFactory.decodeStream(arrayInputStream);
    }
}
