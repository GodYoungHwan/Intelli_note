package droid.com.intelli_note;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NextActivity extends AppCompatActivity {
    private Manager mgr=Manager.getInstance();
    private SQLiteDatabase db;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VOICE = 2;
    static final int REQUEST_OCR = 3;
    private final int PICK_FROM_ALBUM = 66; //갤러리 접근코드
    private boolean isSaved;
    private boolean isNew;
    private final String databaseName="Intelli_Note";
    private final String pictableName="picture";
    private final String tableName="memo";
    private int index;
    private String content;
    private String date;
    private byte[] picture;
    private int picture_num;
    SimpleDateFormat dateformat=new SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm:ss");
    private String before;
    private String after;
    private int first_picNum;
    private int start;
    private int finish;
    private int count;
    private boolean textChange;

    EditText tv = null;

    ImageButton camerabt = null;
    ImageButton voicebt = null;
    ImageButton ocrbt = null;
    ImageButton imagebt = null;
    ImageButton savebt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        db=openOrCreateDatabase(databaseName,MODE_ENABLE_WRITE_AHEAD_LOGGING,null);

        isSaved=true;
        isNew=false;

        camerabt = (ImageButton)findViewById(R.id.camerabutton);
        voicebt = (ImageButton)findViewById(R.id.voicebutton);
        ocrbt = (ImageButton)findViewById(R.id.ocrbutton);
        imagebt = (ImageButton)findViewById(R.id.imagebutton);
        savebt = (ImageButton)findViewById(R.id.savebutton);
        tv = (EditText)findViewById(R.id.tv);
        picture=null;


        Intent intent=getIntent();
        index=intent.getIntExtra("index",-1);//index
        if(index==-1)//새로 추가된 데이터
        {
            isNew = true;
            Cursor count = db.rawQuery("select * from " + tableName, null);
            index = count.getCount() + 1;
        }
        else{//기존에 저장된 데이터
            String SQL="select content, picture_num"
                    +" from "+tableName
                    +" where _id="+index;

            Cursor c=db.rawQuery(SQL,null);

            if(c.moveToFirst()){
                tv.setText(c.getString(0));
                picture_num =c.getInt(1);
                Log.e("adsfasdfasdfsad","Asdfasdfasdfsadfasdfsadfsdaf");
                first_picNum=picture_num;
                Log.e("pic_num",Integer.toString(picture_num));
                picLoad();
            }
        }

        camerabt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cam,1);
                isSaved=false;
            }
        });

        savebt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                content=tv.getText().toString();
                date=dateformat.format(new Date());
                if(isNew) //새로운 메모
                {
                    ContentValues values=new ContentValues();
                    values.put("content",content);
                    values.put("date",date);
                    values.put("picture_num",picture_num);
                    db.insert(tableName,null,values);
                }
                else//기존 메모 업데이트
                {
                    ContentValues values=new ContentValues();
                    values.put("content",content);
                    values.put("date",date);
                    values.put("picture_num",picture_num);
                    db.update(tableName,values,"_id="+index,null);
                }
                finish();
            }
        });

        imagebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGallery();
            }
        });
        ocrbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ocr = new Intent();
                ocr.setClass(NextActivity.this,OCR.class);
                ocr.putExtra("index",index);
                startActivityForResult(ocr,3);
            }
        });
        voicebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent voice = new Intent();
                voice.setClass(NextActivity.this,Voice.class);
                startActivityForResult(voice,2);
            }
        });

        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before=s.toString();
                NextActivity.this.count=count;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSaved=false;
                if(s==null) after="";
                else after=s.toString();
                NextActivity.this.start=start;
                NextActivity.this.finish=finish;
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("before",before);
                Log.e("after",after);
                if (before.length() > after.length()&&count>4&&textChange==false) {//사진을 지우는 경우
                    Log.e("삭제 문자",before.substring(start,start+count));
                    int index=before.substring(start,start+count).replace("(사진","").indexOf(")");
                    int del_idx = Integer.parseInt(before.substring(start,start+count).replace("(사진", "").substring(0,index));
                    picture_num--;
                    Log.e("del_idx",Integer.toString(del_idx));
                    db.delete(pictableName,"idx="+del_idx,null);

                    db.execSQL("update " + pictableName + " set idx=idx-1 where _id=" + index + " and idx>" + del_idx+";");
                    editIndex(del_idx);

                        }
                    }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public void editIndex(int del_idx){
        if(textChange==false) {
            textChange = true;

            int st=0;
            int et=0;
            for(int i=0;i<tv.getText().toString().length();i++){

                if (Character.isDigit(tv.getText().toString().charAt(i))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(tv.getText().toString().charAt(i));
                    st=i;
                    et=i+1;
                    for(int j=i+1;j<tv.getText().toString().length();j++){
                        if (Character.isDigit(tv.getText().toString().charAt(j))) {
                            sb.append(tv.getText().toString().charAt(j));
                            et=j;
                        }else{
                            int pic_idx = Integer.parseInt(sb.toString());
                            //숫자부분 변경
                            if (pic_idx > del_idx) {
                                pic_idx--;
                                tv.getText().replace(st, et, Integer.toString(pic_idx));
                            }
                            i=j+1;
                            break;
                        }
                    }
                }

            }
        }
        textChange=false;
    }

    public void picLoad(){
        Cursor c= db.rawQuery("select idx, picture from "+pictableName
                +" where _id="+index+";",null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            getPicture(c.getInt(0),c.getBlob(1));
            c.moveToNext();
        }
        tv.setTextSize(18);
    }

    public void getPicture(int idx, byte[] picture){
        Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);

        Spannable span = tv.getText();
        int st_index=tv.getText().toString().indexOf("(사진"+idx+")");
        int et_index=tv.getText().toString().indexOf("(사진"+idx+")")+(4+getDigit(idx));
        span.setSpan(new ImageSpan(resizeBitmap(bitmap)), st_index, et_index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setTextSize(18);
    }

    @Override
    public void onBackPressed() {
        if(!isSaved){
            //저장 혹은 저장 안함 혹은 닫기 context 보여주기
            new AlertDialog.Builder(this)
                    .setTitle("변경사항을 저장할까요?")
                    .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //picture 저장한 것 지우기

                            db.delete(pictableName,"idx>="+first_picNum,null);
                            ContentValues values=new ContentValues();
                            values.put("picture_num",first_picNum);
                            db.update(tableName,values,"_id="+index,null);
                            finish();
                        }
                    })
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            content = tv.getText().toString();
                            date = dateformat.format(new Date());
                            if (isNew){ //새로운 메모
                                ContentValues values=new ContentValues();
                                values.put("content",content);
                                values.put("date",date);
                                values.put("picture_num",picture_num);
                                db.insert(tableName,null,values);
                            }
                            else {//기존 메모 업데이트
                                ContentValues values=new ContentValues();
                                values.put("content",content);
                                values.put("date",date);
                                values.put("picture_num",picture_num);
                                db.update(tableName,values,"_id="+index,null);
                            }

                            finish();
                        }
                    }).show();

            return ;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            int st_index = tv.getSelectionStart();
            tv.getText().insert(st_index, "(사진"+picture_num+")");
            int et_index = tv.getSelectionEnd();
            Spannable span = tv.getText();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            picture=bitmapToByteArray(imageBitmap);
            ContentValues values=new ContentValues();
            values.put("_id",index);
            Log.e("pic_num",Integer.toString(picture_num));
            values.put("idx",picture_num++);
            values.put("picture",picture);
            db.insert(pictableName,null,values);
            span.setSpan(new ImageSpan(resizeBitmap(imageBitmap)), st_index, et_index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setTextSize(18);
        }
        else if(requestCode == REQUEST_VOICE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            //tv.setText(tv.getText()+(String)extras.get("text"));
            tv.append((String)extras.get("text"));
        }
        else if(requestCode == REQUEST_OCR && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            tv.setText(tv.getText()+(String)extras.get("text"));
            picLoad();
        }
        else if(requestCode == PICK_FROM_ALBUM){
            Uri uri = data.getData();
            String s = uri.toString();
            Log.e("uri",s);
            //Picasso.with(this).load(uri).centerInside().resize(500,500).into(photo);
            //  Bundle extras = data.getExtras();
            int st_index = tv.getSelectionStart();
            tv.getText().insert(st_index, "(사진"+picture_num+")");
            int et_index = tv.getSelectionEnd();
            Spannable span = tv.getText();
            Bitmap imageBitmap = null;//(Bitmap)extras.get("data");
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(NextActivity.this.getContentResolver(),uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }
            imageBitmap=rotate(imageBitmap,exifDegree);
            picture=bitmapToByteArray(imageBitmap);
            ContentValues values=new ContentValues();
            values.put("_id",index);
            values.put("idx",picture_num++);
            values.put("picture",picture);
            Log.e("pic_num",Integer.toString(picture_num));
            db.insert(pictableName,null,values);
            span.setSpan(new ImageSpan(resizeBitmap(imageBitmap)), st_index, et_index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setTextSize(18);
        }
        else{
            return;
        }
    }

    static public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = 1500;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        double targetHeight =  resizeWidth * aspectRatio;
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, (int) targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }

    public byte[] bitmapToByteArray( Bitmap bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress( Bitmap.CompressFormat.PNG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }


    public int getDigit(int picture_num){
        if(picture_num==0)return 1;
        int digit=0;
        while(picture_num>0){
            picture_num/=10;
            digit++;
        }
        return digit;
    }
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
}