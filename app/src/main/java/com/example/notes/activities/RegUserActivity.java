package com.example.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.example.notes.R;

import com.example.notes.Util.MyBitmapUtil;
import com.example.notes.entities.RegUserResult;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.google.gson.Gson;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.makeramen.roundedimageview.RoundedImageView;

public class RegUserActivity extends AppCompatActivity {

    private EditText regUser_name;
    private EditText password1;
    private EditText password2;
    private Button regUser;
    private String post_to_pic = null;
    private RoundedImageView userImage;
    private String selectedImagePath;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    String obj = (String) msg.obj;
                    Gson gson = new Gson();
                    RegUserResult regUserResult = gson.fromJson(obj,RegUserResult.class);
                    if (regUserResult.getStatus()!=0){
                        Toast.makeText(RegUserActivity.this, regUserResult.getMessage(), Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent();
                        intent.setClass(RegUserActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reguser);
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.colorGray));
        BarUtils.setNavBarVisibility(this,false);
        InitView();

    }

    private void InitView() {
        regUser_name = findViewById(R.id.regUser_name);
        password1 = findViewById(R.id.regUser_password1);
        password2 = findViewById(R.id.regUser_password2);
        regUser = findViewById(R.id.regUser);
        userImage = findViewById(R.id.regUser_image);

        //上传头像
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED){
                    //传存储权限数据给onRequestPermissionsResult
                    ActivityCompat.requestPermissions(
                            RegUserActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else {
                    selectImage();
                }
            }


        });
        //注册按钮监听
        regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String picture_code;
                String admin = regUser_name.getText().toString();
                String password = password1.getText().toString();
                String password_confirm = password2.getText().toString();
                //判断是否选择图片
                if (TextUtils.isEmpty(post_to_pic)){
                    Bitmap bitmap = MyBitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.user_image));
                    picture_code = "data:image/png;base64,"+MyBitmapUtil.bitmapToString(bitmap);
                }else {
                    picture_code = post_to_pic;
                }
                Log.d("mmm",picture_code);

                if (!password.equals(password_confirm)){
                    Toast.makeText(RegUserActivity.this, "密码不一致，请重新输入!", Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            try {
                                PostRequest postRequest = new PostRequest();
                                String result = postRequest.post("http://152.136.246.142:3007/api/reguser", admin, password, picture_code);
                                if (myJsonUtil.isJson(result)){
                                    Message message=mHandler.obtainMessage();
                                    message.what=1;
                                    message.obj=result;
                                    mHandler.handleMessage(message);
                                }else {
                                    Toast.makeText(RegUserActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            //传所选择的图片数据给onActivityResult
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION&&grantResults.length>0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                Toast.makeText(getApplicationContext(), "请先开启读取存储卡权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE&& resultCode == RESULT_OK){
            if (data!=null){
                Uri ImageUri = data.getData();
                if (ImageUri != null) {
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(ImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        userImage.setImageBitmap(bitmap);

                        selectedImagePath = getPathFromUri(ImageUri);
                        post_to_pic = "data:image/png;base64,"+MyBitmapUtil.bitmapToString(selectedImagePath);


                    }catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public String   getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }


}