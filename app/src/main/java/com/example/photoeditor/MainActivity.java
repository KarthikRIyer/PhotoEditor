package com.example.photoeditor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //declaring necesary variables
    public static final int MY_PERMISSION_REQUEST_CAMERA = 100,REQUEST_IMAGE_CAPTURE=1;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    public static final int MY_PERMISSION_REQUEST_STORE = 99;
    public static final String STORE_PREF = "store_pref";
    public static String mCurrentPhotoPath;
    public final int RESULT_LOAD_IMAGE =20;
    public Uri pickedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermG();

        FloatingActionButton addFab = (FloatingActionButton)findViewById(R.id.addFab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String items[] = {"Capture Image from Camera","Choose Image from Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert);
                builder.setTitle("Add Photo!");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(items[i].equals("Capture Image from Camera")){checkPermC();}
                        else if(items[i].equals("Choose Image from Gallery")){galleryIntent();}
                        else{dialogInterface.dismiss();}
                    }
                });
                builder.show();
            }
        });
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed){
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key,allowed);
        prefsEditor.commit();
    }

    public static boolean getFromPref(Context context,String key){
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key,false));
    }

    private void showAlert(){

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera!");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                finish();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CAMERA);
            }
        });
        alertDialog.show();


    }

    private void showSettingsAlert(){

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults){

        switch (requestCode){
            case MY_PERMISSION_REQUEST_CAMERA:
                for(int i=0,len=permissions.length;i<len;i++){
                    String permission = permissions[i];
                    if(grantResults[i]== PackageManager.PERMISSION_DENIED){
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,permission);
                        if(showRationale){showAlert();}
                        else if(!showRationale){
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(MainActivity.this,ALLOW_KEY,true);
                        }
                    }
                }
                break;
            case MY_PERMISSION_REQUEST_STORE:
                for(int i=0,len=permissions.length;i<len;i++){
                    String permission = permissions[i];
                    if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,permission);
                        if(showRationale){showAlert();}
                        else if(!showRationale){
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferencesG(MainActivity.this,ALLOW_KEY,true);
                        }
                    }
                }
        }
    }


    protected void onResume(){
        super.onResume();
    }

    //function to open camera
    private void openCamera(){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        if(intent.resolveActivity(getPackageManager())!=null){

            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e){
                Log.d("","Couldn't create File");}

            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.photo_editor_fileprovider",photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
            }

        }



    }

    //function called on getting image either from camera or gallery. Gets file path and starts activity to diplay image
    @Override
    protected void onActivityResult(int requestCode,int ResultCode,Intent data){

        if(requestCode == REQUEST_IMAGE_CAPTURE && ResultCode == Activity.RESULT_OK){

            Intent i = new Intent(this,Image_Display_Activity.class);
            int H = this.getWindow().getDecorView().getHeight();
            int W = this.getWindow().getDecorView().getWidth();
            i.putExtra("height",H);
            i.putExtra("width",W);
            startActivity(i);
        }
        else if(requestCode == RESULT_LOAD_IMAGE && ResultCode == Activity.RESULT_OK){

            pickedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage,filePath,null,null,null);
            cursor.moveToFirst();
            mCurrentPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            Intent i = new Intent(this,Image_Display_Activity.class);
            int H = this.getWindow().getDecorView().getHeight();
            int W = this.getWindow().getDecorView().getWidth();
            i.putExtra("height",H);
            i.putExtra("width",W);
            startActivity(i);

        }

    }

    //function to create a file to store the image. It creates new file name with time stamp
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //check if app has permission to open camera. open if yes
    public void checkPermC(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            if(getFromPref(this,ALLOW_KEY)){showSettingsAlert();}
            else if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                //should we show an explanation?
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){showAlert();}
                else {
                    //no explanation needed, we can request the permission
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CAMERA);
                }
            }
        }
        else {openCamera(); }

    }

    //function to open gallery to pick image
    public void galleryIntent(){

        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i,RESULT_LOAD_IMAGE);

    }

    //function to check if app has access to storage
    public void checkPermG(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(getFromPrefG(this,ALLOW_KEY)){showSettingsAlertG();}
            else if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                //should we show an explanation?
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){showAlertG();}
                else {
                    //no explanation needed, we can request the permission
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_STORE);
                }
            }
        }

    }

    public static boolean getFromPrefG(Context context, String key){
        SharedPreferences myPrefs = context.getSharedPreferences(STORE_PREF,Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key,false));
    }

    public static void saveToPreferencesG(Context context,String key,Boolean allowed){
        SharedPreferences myPrefs = context.getSharedPreferences(STORE_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key,allowed);
        prefsEditor.commit();
    }

    private void showAlertG(){

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access storage!");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                finish();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_STORE);
            }
        });
        alertDialog.show();


    }

    private void showSettingsAlertG(){

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access storage!.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        alertDialog.show();
    }

}
