package jp.ac.hec.cm0107.samplecamerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private ImageCapture imageCapture;
    PreviewView previewView;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button btnCapture;
    private String TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        TAG = "MainActivity";


        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
     //   btnCapture.setOnClickListener(new capture());
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        startCamera();
    }


    private void startCamera() {
        cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        // 余力のある人は「ラムダ式」を調べて導入してみよう
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                imageCapture = new ImageCapture.Builder().build();

                // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) CameraActivity.this),
                        cameraSelector,
                        preview, imageCapture);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());


            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        if (imageCapture == null) return;

        // Create time stamped name and MediaStore entry.
        final String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }
        final Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 共有ストレージのPicturesディレクトリのパスを取得する
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        // Create output options object which contains file + metadata
        final ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        imageCollection,
                        contentValues
                ).build();

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                final String msg = "Photo capture succeeded: " + output.getSavedUri();
                Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exception);
            }
        });
    }
//    private class capture implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            File file = new File(getFilesDir(), getNowDate() + ".jpg");
//            saveScreenShot();
//            ImageCapture.OutputFileOptions outputFileOptions =
//                    new ImageCapture.OutputFileOptions.Builder(file).build();
//            imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(),
//                    new ImageCapture.OnImageSavedCallback() {
//                        @Override
//                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//
//                            Bitmap photo = BitmapFactory.decodeFile(file.getPath());
//                            Bitmap result = combineBitmap(photo,screenShot());
//                            OutputStream out = null;
//                            try {
//                                out = new FileOutputStream(file);
//                                result.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                                out.close();
//                                putGallery(file);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            Log.i(TAG, "onImageSave");
//                            //Log.i(TAG, outputFileResults.getSavedUri().toString());
//                        }
//
//                        @Override
//                        public void onError(@NonNull ImageCaptureException error) {
//                            Log.i(TAG, "onError");
//                            Log.i(TAG, error.getMessage());
//                        }
//                    });
//        }
//    }

    public static String getNowDate() {
        @SuppressLint("SimpleDateFormat") final DateFormat df =
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    private void putGallery(File file) {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "photo.jpg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        // imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        try {
            OutputStream fos = resolver.openOutputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Bitmap screenShot() {
        Bitmap retBitmap = null;
        ViewGroup rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        View frameView = null;
        if (rootView != null) {
            frameView = rootView.getChildAt(0);
        }
        if (frameView != null) {
            frameView.setDrawingCacheEnabled(true);
            retBitmap = Bitmap.createBitmap(frameView.getDrawingCache());
            frameView.setDrawingCacheEnabled(false);
        }
        return retBitmap;
    }

    private void saveScreenShot() {
        final String fileName = "screenshot.jpg";
        // final String tmpFileName = "temp.jpg";

        File file = new File(getFilesDir(), fileName);
        Bitmap bitmap = screenShot();

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap combineBitmap(Bitmap b1,Bitmap b2){
        Bitmap result = Bitmap.createBitmap(b1.getWidth(),b1.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas =  new Canvas(result);
        canvas.drawBitmap(b1,0,0,null);
        b1.recycle();

        Rect srcRect = new Rect(0,0,b2.getWidth(),b2.getHeight());
        Rect dstRect = new Rect(0,0,result.getWidth(),result.getHeight());
        canvas.drawBitmap(b2,srcRect,dstRect,null);
        b2.recycle();
        return result;
    }
}
