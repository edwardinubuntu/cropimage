package eu.janmuller.android.simplecropimage;

import android.os.Bundle;
import android.view.View;
import eu.janmuller.android.simplecropimage.view.CropImageView;

/**
 * Created by edward_chiang on 2013/11/21.
 */
public class CropImageSimpleActivity extends CropImageActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.crop_image_save_only);

        mImageView = (CropImageView) findViewById(R.id.image);

        findViewById(R.id.save).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        try {
                            onSaveClicked();
                        } catch (Exception e) {
                            finish();
                        }
                    }
                });

        startFaceDetection();
    }
}
