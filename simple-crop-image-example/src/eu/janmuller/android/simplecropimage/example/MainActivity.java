package eu.janmuller.android.simplecropimage.example;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import eu.janmuller.android.simplecropimage.CropImageActivity;
import eu.janmuller.android.simplecropimage.CropImageSimpleActivity;

import java.io.*;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
    
    public static final int REQUEST_CODE_GALLERY      = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;

    private ImageView mImageView;
    private File      mFileTemp;

    private CropImageActivity.CropStyle cropStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.main);

        findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGallery();
            }
        });

        findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                takePicture();
            }
        });

        findViewById(R.id.square_photo_from_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropStyle = CropImageActivity.CropStyle.SQUARE;
                openGallery();
            }
        });

        findViewById(R.id.square_photo_from_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropStyle = CropImageActivity.CropStyle.SQUARE;
                takePicture();
            }
        });

        mImageView = (ImageView) findViewById(R.id.image);
        
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    		mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
    	}
    	else {
    		mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
    	}

    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
        	Uri mImageCaptureUri = null;
        	String state = Environment.getExternalStorageState();
        	if (Environment.MEDIA_MOUNTED.equals(state)) {
        		mImageCaptureUri = Uri.fromFile(mFileTemp);
        	}
        	else {
	        	/*
	        	 * The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder
	        	 */
	        	mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
        	}	
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {

            Log.d(TAG, "cannot take picture", e);
        }
    }

    private void openGallery() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    private void startCropImage(CropImageActivity.CropStyle cropStyle) {

        Intent intent = new Intent(this, CropImageSimpleActivity.class);
        intent.putExtra(CropImageActivity.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImageActivity.SCALE, true);

        intent.putExtra(CropImageActivity.ASPECT_X, 3);
        intent.putExtra(CropImageActivity.ASPECT_Y, 2);
        intent.putExtra(CropImageActivity.CROP_STYLE, cropStyle);

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {

            return;
        }

        Bitmap bitmap;

        switch (requestCode) {

            case REQUEST_CODE_GALLERY:

                try {

                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();

                    startCropImage(cropStyle);

                } catch (Exception e) {

                    Log.e(TAG, "Error while creating temp file", e);
                }

                break;
            case REQUEST_CODE_TAKE_PICTURE:

                startCropImage(cropStyle);
                break;
            case REQUEST_CODE_CROP_IMAGE:

                String path = data.getStringExtra(CropImageActivity.IMAGE_PATH);
                if (path == null) {

                    return;
                }

                bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
                mImageView.setImageBitmap(bitmap);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

}
