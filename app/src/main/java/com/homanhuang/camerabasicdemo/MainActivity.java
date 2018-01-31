package com.homanhuang.camerabasicdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation") //I don't want to see the crossline all over the code
public class MainActivity extends AppCompatActivity {
    /* Toast shortcut */
    public static void msg(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /* Log tag and shortcut */
    final static String TAG = "MYLOG MainA";
    public static void ltag(String message) {
        Log.i(TAG, message);
    }

    //camera
    private Camera frontCamera = null;
    private Camera rearCamera = null;
    private CameraView frontCameraView = null;
    private CameraView rearCameraView = null;
    boolean frontCameraExist = false;
    boolean rearCameraExist = false;
    Integer frontCameraId = null;
    Integer rearCameraId = null;

    ImageView cameraImageView;

    TextView frontTextView;
    TextView readTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraImageView = (ImageView) findViewById(R.id.cameraImageView);

        frontTextView = (TextView) findViewById(R.id.frontTextView);
        readTextView = (TextView) findViewById(R.id.rearTextView);


        if (Build.VERSION.SDK_INT < 23) {
            //Do not need to check the permission
        } else {
            requestPermissions();
        }

        MyCameraManager myCameraManager = new MyCameraManager(this);

        if (myCameraManager.getNumberOfLens() == 0) {
            ltag("This phone has no camera");
            msg(this, "Camera Not Found");
        } else {
            boolean front = false;
            boolean rear = false;
            if ((frontCameraId = myCameraManager.getFrontCameraId()) != null) {
                ltag("Front camera found!");
                frontTextView.setText("Front Found: "+frontCameraId);
                frontCameraExist = true;
            }
            if ((rearCameraId = myCameraManager.getRearCameraId()) != null) {
                ltag("Rear camera found");
                readTextView.setText("Rear Found: "+rearCameraId);
                rearCameraExist = true;
            }
            if (frontCameraExist && rearCameraExist) {
                msg(this, "Front and Rear Camera Found!");
            } else {
                if (frontCameraExist) {
                    msg(this, "Front Camera Found!");
                }
                if (rearCameraExist) {
                    msg(this, "Rear Camera Found!");
                }
            }
        }


    }

    /*
        Check permissions for camera and external storage
     */
    static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 200; // any code you want.
    public void requestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            ltag("Permission is granted");
        } else {

            ltag("Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    /*
        Create image file by date and time.
     */
    String mCurrentPhotoPath;
    String imageFileName;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";

        //Album
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //create full size image
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /*
        use intent to capture the image
    */
    //camera permit
    static final int REQUEST_IMAGE_CAPTURE = 100;
    Uri photoURI;
    public void capture(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ltag("error: "+ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.homanhuang.camerabasicdemo.fileprovider",
                        photoFile);
                ltag("photoURI: "+photoURI.getPath().toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
            if you use MediaStore.EXTRA_OUTPUT, the data will be null.
         */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ContentResolver contentResolver = this.getContentResolver();
            try {
                //this is full size image
                Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoURI);

                if (fullBitmap.getWidth() > fullBitmap.getHeight()) {
                    fullBitmap = rotate90(fullBitmap);
                }

                cameraImageView.setImageBitmap(fullBitmap);

                cameraImageView.setVisibility(View.VISIBLE);

                saveImage(fullBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
        Save full size image
     */
    private void saveImage(Bitmap bitmap) {

        try {
            String albumName = "CameraDemoAlbum";
            File albumDir = getAlbumStorageDir(albumName);

            OutputStream imageOut = null;
            File file = new File(albumDir, imageFileName+".jpg");

            imageOut = new FileOutputStream(file);

            //Bitmap -> JPEG with 85% compression rate
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, imageOut);
            imageOut.flush();
            imageOut.close();

            //update gallery so you can view the image in gallery
            updateGallery(albumName, albumDir, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Add the image and image album into gallery
     */
    private void updateGallery(String albumName, File albumDir, File file) {
        //metadata of new image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, imageFileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, albumName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        ltag("album Directory: "+albumDir.getAbsolutePath());

        File f = new File(albumDir.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        //notify gallery for new image
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);
    }

    /*
        Set the personal album in DCIM
     */
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
        if (!file.mkdirs()) {
            ltag("Directory not created");
        }
        return file;
    }

    /*
        Open the gallery in the phone
     */
    static final int RESULT_GALLERY = 500;
    public void openGallery(View view) {
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent , RESULT_GALLERY );
    }

    /*
        Rotate 90 degree if the image is in landscape.
     */
    public Bitmap rotate90(Bitmap paramBitmap)
    {
        int rotateAngle = 90;
        Matrix localMatrix = new Matrix();
        float f1 = paramBitmap.getWidth() / 2;
        float f2 = paramBitmap.getHeight() / 2;
        localMatrix.postTranslate(-paramBitmap.getWidth() / 2, -paramBitmap.getHeight() / 2);
        localMatrix.postRotate(rotateAngle);
        localMatrix.postTranslate(f1, f2);
        paramBitmap = Bitmap.createBitmap(paramBitmap, 0, 0, paramBitmap.getWidth(), paramBitmap.getHeight(), localMatrix, true);
        new Canvas(paramBitmap).drawBitmap(paramBitmap, 0.0F, 0.0F, null);
        return paramBitmap;
    }

    private TextureView frontTextureView;
    private void previewCameraFront() {

        try{
            frontCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            ltag("Failed to get camera: " + e.getMessage());
        }

        if(frontCamera != null) {
            frontCameraView = new CameraView(this, frontCamera);//create a SurfaceView to show camera data
            frontTextureView = (TextureView) findViewById(R.id.frontTextureView);
        }
    }

    private TextureView rearTextureView;
    private void previewCameraRear() {

        try{
            rearCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            ltag("Failed to get camera: " + e.getMessage());
        }

        if(rearCamera != null) {
            rearCameraView = new CameraView(this, rearCamera);//create a SurfaceView to show camera data
            rearTextureView = (TextureView) findViewById(R.id.rearTextureView);
        }
    }
}
