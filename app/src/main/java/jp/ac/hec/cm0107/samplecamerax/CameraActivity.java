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
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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

public class CameraActivity extends AppCompatActivity implements View.OnTouchListener{

    private ImageCapture imageCapture;
    private PreviewView previewView;


    ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private Button btnCapture;
    private Button btnClose;
    private String TAG ;
    private int screenX;
    private int screenY;
    private ImageView target;
    private int cameraMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_camera);

        target = findViewById(R.id.imageView);
        target.setOnTouchListener(this);

        previewView = findViewById(R.id.previewView);

        btnCapture = findViewById(R.id.btnCapture);
        TAG = "MainActivity";
        Intent intent = getIntent();
        cameraMode = intent.getIntExtra(MainActivity.EXTRA_DATA,0);
        if ( cameraMode == 2 ) { // セリフを消す
            ViewGroup rootView = (ViewGroup) getWindow().
                    getDecorView().findViewById(android.R.id.content);
            if (rootView != null && rootView.getChildCount() != 0) {
                ViewGroup frameView = (ViewGroup) rootView.getChildAt(0);
                //View serifView = findViewById(R.id.laySerif);
                //frameView.removeView(serifView);
            }
        } else if ( cameraMode == 3 ) { // キャラクタを消す
            ViewGroup rootView = (ViewGroup) getWindow().
                    getDecorView().findViewById(android.R.id.content);
            if (rootView != null && rootView.getChildCount() != 0) {
                ViewGroup frameView = (ViewGroup) rootView.getChildAt(0);
                //View serifView = findViewById(R.id.imgChara);
                //frameView.removeView(serifView);
            }
        }

        startCamera();

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getFilesDir(),"photo1.jpg");
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap photo = BitmapFactory.decodeFile(file.getPath());
                        Bitmap result = combineBitmap(photo,screenShot());

                        putGallery(file);
                        OutputStream out  = null;
                        try{
                            out = new FileOutputStream(file);
                            result.compress(Bitmap.CompressFormat.JPEG,100,out);
                            out.close();
                            putGallery(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        Log.i(TAG,"onImageSaved");
                        Log.i(TAG,outputFileResults.getSavedUri().toString());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.i(TAG,"onError");
                        Log.i(TAG,exception.getMessage());
                    }
                });
//                saveScreenShot();
            }
        });


    }



    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
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
                            (LifecycleOwner) CameraActivity.this,
                            cameraSelector,
                            preview, imageCapture);

                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(
                            previewView.getSurfaceProvider());

                }catch (InterruptedException | ExecutionException e) {
                    // Currently no exceptions thrown. cameraProviderFuture.get()
                    // shouldn't block since the listener is being called, so no need to
                    // handle InterruptedException.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void putGallery(File file){
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"photo.jpg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        try{
            OutputStream fos = resolver.openOutputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap screenShot(){
        Bitmap retBitmap = null;
        ViewGroup rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        View frameView = null;

        if(rootView != null){
            frameView = rootView.getChildAt(0);
        }

        if (frameView != null){
            frameView.setDrawingCacheEnabled(true);
            retBitmap = Bitmap.createBitmap(frameView.getDrawingCache());
            frameView.setDrawingCacheEnabled(false);
        }
        return retBitmap;
    }

    private void saveScreenShot(){
        final String fileName = "screenshot.jpg";
        final String tmpFileName = "temp.jpg";

        File file = new File(getFilesDir(), fileName);

        Bitmap bitmap = screenShot();

        ImageCapture.OutputFileOptions outputFileOptions= new ImageCapture.OutputFileOptions.Builder(file).build();

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.close();
            putGallery(file);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap combineBitmap(Bitmap b1,Bitmap b2){
        Bitmap result = Bitmap.createBitmap(b1.getWidth(),b1.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(b1,0,0,null);
        b1.recycle();

        Rect srcRect = new Rect(0,0,b2.getWidth(),b2.getHeight());
        Rect dstRect = new Rect(0,0,result.getWidth(),result.getHeight());

        canvas.drawBitmap(b2,srcRect,dstRect,null);

        b2.recycle();
        return result;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                int dx = target.getLeft() + (x - screenX);
                int dy = target.getTop() + (y - screenY);
                target.layout(dx,dy,
                        dx + target.getWidth(),
                        dy + target.getHeight());
                break;
        }
        screenX = x;
        screenY = y;
        return true;
    }
}