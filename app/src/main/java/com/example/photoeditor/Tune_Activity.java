package com.example.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tune_Activity extends AppCompatActivity {

    Bitmap textBit = Image_Display_Activity.bm;
    float cont = 1f;
    float bright = 0f;
    float sat = 1f;
    ImageView tuneDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tuneDisplay = (ImageView)findViewById(R.id.tunedDisplay);
        tuneDisplay.setImageBitmap(textBit);

        if(getIntent().getExtras().getFloat("iHeight") > getIntent().getExtras().getFloat("height")- 400){
            tuneDisplay.getLayoutParams().height = (int)(getIntent().getExtras().getFloat("height")- 400);
            tuneDisplay.requestLayout();
        }

        ((SeekBar)findViewById(R.id.brightnessBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bright = ((255f/50f)*i)-255f;
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((SeekBar)findViewById(R.id.contrastBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cont = i*(0.1f);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        ((SeekBar)findViewById(R.id.saturationBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sat = (float)i/256f;
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((Button)findViewById(R.id.resetBrightness)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bright = 0f;
                ((SeekBar)findViewById(R.id.brightnessBar)).setProgress(50);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });
        ((Button)findViewById(R.id.resetContrast)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cont = 1f;
                ((SeekBar)findViewById(R.id.contrastBar)).setProgress(10);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });
        ((Button)findViewById(R.id.resetSaturation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sat = 1f;
                ((SeekBar)findViewById(R.id.saturationBar)).setProgress(256);
                tuneDisplay.setImageBitmap(changeBitmapContrastBrightness(cont,bright,sat));
            }
        });
        ((Button)findViewById(R.id.save_changes_button_tune)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
            }
        });
        ((ImageView)findViewById(R.id.saveTuneIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });
        ((ImageView)findViewById(R.id.cancelTuneIcon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

    private void saveBitmap(){
        Image_Display_Activity.bm = ((BitmapDrawable)tuneDisplay.getDrawable()).getBitmap();;
        (Image_Display_Activity.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
        Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
    }
    private void saveImage()throws Exception{
        saveBitmap();
        FileOutputStream fOut = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);

        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){e.printStackTrace();}
        (Image_Display_Activity.bm).compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.flush();
        }catch (Exception e){e.printStackTrace();}
        try{fOut.close();}catch (IOException e){e.printStackTrace();}
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());}
        catch (FileNotFoundException e){e.printStackTrace();}

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image Saved",Toast.LENGTH_SHORT).show();
    }


    private Bitmap changeBitmapContrastBrightness(float contrast, float brightness,float saturation)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(textBit.getWidth(), textBit.getHeight(), textBit.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(textBit, 0, 0, paint);
        cm.setSaturation(saturation);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(ret,0,0,paint);
        return ret;
    }
}
