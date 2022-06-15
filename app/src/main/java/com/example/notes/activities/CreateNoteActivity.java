package com.example.notes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.example.notes.R;
import com.example.notes.Util.MyBitmapUtil;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
public class CreateNoteActivity extends AppCompatActivity {


    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView inputTextDateTime;
    //副标题颜色
    private View viewSubtitleIndicator;
    private String selectedNoteColor;
    private String selectedImagePath;

    private ImageView imageNote;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;

    private SharedPreferences sharedPreferences;
    private ImageView uploadNote;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private String base64_to_cloud;


    //添加url对话框
    private AlertDialog dialogAddUrl;

    //删除确认框
    private AlertDialog dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(view -> {onBackPressed();});

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        inputTextDateTime = findViewById(R.id.inputTextDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        layoutWebUrl = findViewById(R.id.layoutWebUrl);
        uploadNote = findViewById(R.id.UploadNote);

        inputTextDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault())
                      .format(new Date())
        );

        ImageView  imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener((v) -> { saveNote(); });

        //初始颜色
        selectedNoteColor = "#333333";
        selectedImagePath = "";
        base64_to_cloud="";

        if (getIntent().getBooleanExtra("isViewOrUpdate",false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        //删除链接
        findViewById(R.id.imageRemoveWebUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                layoutWebUrl.setVisibility(View.GONE);
            }
        });
        //删除图片
        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageDrawable(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
//        String now_time =  new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date());
        inputTextDateTime.setText(alreadyAvailableNote.getDateTime());
        uploadNote.setVisibility(View.VISIBLE);
        uploadNote.setOnClickListener((v) -> { uploadNote(); });
        //修改图片
        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            Log.d("base87",alreadyAvailableNote.getImagePath());
            String temp_path = alreadyAvailableNote.getImagePath();
            if (!temp_path.startsWith("data")) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(temp_path));
            } else {
                temp_path = temp_path.replace("data:image/png;base64,", "");
                imageNote.setImageBitmap(MyBitmapUtil.base64ToBitmap(temp_path));
            }
            imageNote.setVisibility(View.VISIBLE);

            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

            selectedImagePath = alreadyAvailableNote.getImagePath();
        }
        //修改url
        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            textWebUrl.setText(alreadyAvailableNote.getWebLink());
            layoutWebUrl.setVisibility(View.VISIBLE);
        }
    }

    private void uploadNote() {
        final Note note = setNote();
        //原图上传
        if (!TextUtils.isEmpty(alreadyAvailableNote.getImagePath())) {
            if (!alreadyAvailableNote.getImagePath().startsWith("data")){
                String temp_path = alreadyAvailableNote.getImagePath();
                Log.d("result_now",temp_path);
                base64_to_cloud = "data:image/png;base64," + MyBitmapUtil.bitmapToString(temp_path);
            }else {
                base64_to_cloud = alreadyAvailableNote.getImagePath();
            }
        }
        note.setImagePath(base64_to_cloud);
//        String now_time =  new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date());
        note.setDateTime(alreadyAvailableNote.getDateTime());
        String Note_js = JSON.toJSONString(note);
        PostRequest postRequest = new PostRequest();
        sharedPreferences = getSharedPreferences("login", 0);
        if (TextUtils.isEmpty(sharedPreferences.getString("token", ""))) {
            Toast.makeText(CreateNoteActivity.this, "账号未登录", Toast.LENGTH_SHORT).show();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        String result = postRequest.post_note("http://152.136.246.142:3007/my/upload", sharedPreferences.getString("token", ""), Note_js);
                        if (myJsonUtil.isJson(result)) {
                            Toast.makeText(CreateNoteActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                            Log.d("result_now",result);
                        } else {
                            Toast.makeText(CreateNoteActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                            Log.d("result_now",result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //保存笔记
    public void saveNote() {
        if (inputNoteTitle.getText().toString().isEmpty()) {
            Toast.makeText(this, "文章标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }else if (inputNoteSubtitle.getText().toString().isEmpty()
        && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note =setNote();
        note.setImagePath(selectedImagePath);

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insert(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private Note setNote() {
        final Note note = new Note();
        //保存数据
        UUID id = UUID.randomUUID();
        note.setId(id.toString());
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        //修改时的获取时间
        note.setDateTime(inputTextDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        //如果layoutWebUrl可见，则url肯定添加了
        if (layoutWebUrl.getVisibility() == View.VISIBLE) {
            //保存链接
            note.setWebLink(textWebUrl.getText().toString());
        }else {
            note.setWebLink("");
        }

        if (alreadyAvailableNote != null) {
            //修改笔记
            note.setId(alreadyAvailableNote.getId());
        }
        return note;
    }

    //样式栏
    private void initMiscellaneous() {
      final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
      final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
      layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
              }else {
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              }
          }
      });

      final  ImageView imageColor1 = findViewById(R.id.imageColor1);
      final  ImageView imageColor2 = findViewById(R.id.imageColor2);
      final  ImageView imageColor3 = findViewById(R.id.imageColor3);
      final  ImageView imageColor4 = findViewById(R.id.imageColor4);
      final  ImageView imageColor5 = findViewById(R.id.imageColor5);
      //颜色样式选中逻辑
      layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              selectedNoteColor = "#333333";
              imageColor1.setImageResource(R.drawable.done_icon);
              imageColor2.setImageResource(0);
              imageColor3.setImageResource(0);
              imageColor4.setImageResource(0);
              imageColor5.setImageResource(0);
              setSubtitleIndicatorColor();
          }
      });
      layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor2.setImageResource(R.drawable.done_icon);
                imageColor1.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
      layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor3.setImageResource(R.drawable.done_icon);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
      layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52FC";
                imageColor4.setImageResource(R.drawable.done_icon);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
      layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor5.setImageResource(R.drawable.done_icon);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

      if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().isEmpty()) {
          switch (alreadyAvailableNote.getColor()) {
              case "#333333":
                  layoutMiscellaneous.findViewById(R.id.viewColor1).performClick();
                  break;
              case "#FDBE3B":
                  layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                  break;
              case "#FF4842":
                  layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                  break;
              case "#3A52FC":
                  layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                  break;
              case "#000000":
                  layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                  break;
          }
      }

      //添加图片
      layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              //判断是否有权限
              if (ContextCompat.checkSelfPermission(
                      getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
              )!= PackageManager.PERMISSION_GRANTED){
                  ActivityCompat.requestPermissions(
                          CreateNoteActivity.this,
                          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                          REQUEST_CODE_STORAGE_PERMISSION
                  );
              }else {
                  selectImage();
              }
          }
      });

      //添加URL
      layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              showAddUrlDialog();
          }
      });

        //删除逻辑
        // alreadyAvailableNote不为空，意味着是修改或者查看状态，显示删除按钮
      if (alreadyAvailableNote != null) {
          //显示删除按键
          layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
          layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                  showDeleteNoteDialog();
              }
          });


      }
    }


    //显示删除确认框
    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            //删除数据库中的数据
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();

    }

    //设置副标题颜色
    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    //选择图片
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getPackageManager()) != null) {
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
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                Log.d("mmm",selectedImageUri.toString());
                if (selectedImageUri != null) {
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        //显示删除图片按钮
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);


                    }catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    //获取文件路径
    private String getPathFromUri(Uri uri) {
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

    //显示添加url对话框
    private void showAddUrlDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();

            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputUrl = view.findViewById(R.id.inputUrl);

            inputUrl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //判断输入的url是否为空
                    if (inputUrl.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "请输入url", Toast.LENGTH_SHORT).show();
                    }
                    //判断输入的url是否合法
                    else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(getApplicationContext(), "请输入正确的url", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        textWebUrl.setText(inputUrl.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }

}