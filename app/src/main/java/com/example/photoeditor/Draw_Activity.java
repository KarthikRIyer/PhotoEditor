package com.example.photoeditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Draw_Activity extends AppCompatActivity implements ColorPickerDialogListener{

    private DrawingView dv;
    private Paint mPaint;
    private float vH=0,vW=0;
    int colorCode = 0xFFFF0000;
    private static final int DIALOG_ID = 0;
    Bitmap textBit = Image_Display_Activity.bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(R.layout.activity_draw_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        float targetW = getIntent().getExtras().getFloat("width");
        float targetH = getIntent().getExtras().getFloat("height");

        {vH = targetH*(0.89f);
            vW = (targetH*(0.89f) / ((Image_Display_Activity.bm).getHeight())) * ((Image_Display_Activity.bm).getWidth());}
        if(vW>targetW){vW = targetW;vH=(targetW/((Image_Display_Activity.bm).getWidth()))*((Image_Display_Activity.bm).getHeight());}



        dv = new DrawingView(this);
        dv.setBackground(new BitmapDrawable(getResources(),Image_Display_Activity.bm));

        dv.setLayoutParams(new ViewGroup.LayoutParams((int)vW,(int)vH));
        ((LinearLayout)findViewById(R.id.view_drawing_pad_draw)).addView(dv);





        ImageView colorDrawIcon = (ImageView)findViewById(R.id.colorOptionDraw);
        colorDrawIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_ID)
                        .setColor(colorCode)
                        .setShowAlphaSlider(true)
                        .show(Draw_Activity.this);
            }
        });
        ImageView saveDrawIcon = (ImageView)findViewById(R.id.saveOptionDraw);
        saveDrawIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });
        ImageView cancelIcon = (ImageView)findViewById(R.id.cancelOptionDraw);
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        Button saveChangesButton = (Button)findViewById(R.id.save_changes_button_draw);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Image_Display_Activity.bm = textBit;
                (Image_Display_Activity.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
                Image_Display_Activity.iHeight = textBit.getHeight();
                Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
            }
        });

        Button clearDrawButton = (Button)findViewById(R.id.clearDrawButton);
        clearDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (dv.mCanvas).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
        });

        SeekBar sizeBar = (SeekBar)findViewById(R.id.sizeBarDraw);
        sizeBar.setProgress(12);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mPaint.setStrokeWidth(progress);
                dv.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    private void saveImage()throws Exception{
        saveBitmap();
        Image_Display_Activity.bm = textBit;
        (Image_Display_Activity.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
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
        Toast.makeText(getApplicationContext(),"Image Saved to Pictures",Toast.LENGTH_SHORT).show();

        dv.invalidate();
    }
    //change colorCode after selecting color
    @Override
    public void onColorSelected(int dialogid,int color){
        colorCode = color;
        dv.invalidate();
    }
    @Override
    public void onDialogDismissed(int dialogid){}

    //save bitmap for futher editing
    private void saveBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(dv.getWidth(),dv.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        dv.draw(c);
        textBit = bitmap;
    }



    //custom drawingView for drawing over image
    public class DrawingView extends View{


        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;

        public DrawingView(Context c){
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(colorCode);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);
        }
        @Override
        protected void onSizeChanged(int w,int h,int oldw,int oldh){
            //super.onSizeChanged(w,h,oldw,oldh);
            //super.onSizeChanged(bm.getWidth(),bm.getHeight(),oldw,oldh);
            super.onSizeChanged((int)vW,(int)vH,oldw,oldh);
            mBitmap = Bitmap.createBitmap((int)vW,(int)vH, Bitmap.Config.ARGB_8888);
            //mBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            //mBitmap = bm.copy(Bitmap.Config.ARGB_8888,true);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            mPaint.setColor(colorCode);
            canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
            canvas.drawPath(mPath,mPaint);
        }
        private float mX,mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x,float y){
            mPath.reset();
            mPath.moveTo(x,y);
            mX = x;
            mY = y;
        }
        private void touch_up(){
            mPath.lineTo(mX,mY);
            mCanvas.drawPath(mPath,mPaint);
            mPath.reset();
        }
        private void touch_move(float x,float y){
            float dx = Math.abs(x-mX);
            float dy = Math.abs(y-mY);
            if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
                mPath.quadTo(mX,mY,((mX+x)/2),((mY+y)/2));
                mX=x;
                mY=y;
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX();
            float y = event.getY();

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touch_start(x,y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x,y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }

            return true;
        }


    }
}
