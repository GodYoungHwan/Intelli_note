package droid.com.intelli_note;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static droid.com.intelli_note.NextActivity.REQUEST_IMAGE_CAPTURE;

public class OCR extends AppCompatActivity {
    Bitmap image; //사용되는 이미지
    private TessBaseAPI mTess; //Tess API reference
    String datapath = "" ; //언어데이터가 있는 경로
    String lang;
    int REQUEST_IMAGE_CAPTURE = 2;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        imageView = (ImageView)findViewById(R.id.imageView);

        //언어파일 경로
        datapath = getFilesDir()+ "/tesseract/";
        //Tesseract API
        lang = "";

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image=imageBitmap;
            imageView.setImageBitmap(imageBitmap);
        }
    }

    //Process an Image
    public void processImage(View view) {
        if(lang==""){
            Toast.makeText(this,"언어를 먼저 설정하세요.",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String OCRresult = null;
            mTess.setImage(image);
            OCRresult = mTess.getUTF8Text();
            TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
            OCRTextView.setText(OCRresult);
            Intent i = new Intent();
            i.putExtra("text",OCRTextView.getText().toString());
            i.putExtra("index",getIntent().getIntExtra("index",-1));
            setResult(-1,i);
            finish();
        }
    }


    //copy file to device
    private void copyFiles() {
        try{
            String filepath = datapath + "/tessdata/"+lang+".traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/"+lang+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check file on the device
    private void checkFile(File dir) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/"+lang+".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.langChoice) {
            openOptionsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void openOptionsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.message)
                .setNegativeButton(R.string.eng,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                                lang = "eng";
                                //트레이닝데이터가 카피되어 있는지 체크
                                checkFile(new File(datapath + "tessdata/"));
                                mTess = new TessBaseAPI();
                                mTess.init(datapath, lang);
                            }
                        })
                .setPositiveButton(R.string.kor,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                                lang = "kor";
                                //트레이닝데이터가 카피되어 있는지 체크
                                checkFile(new File(datapath + "tessdata/"));
                                mTess = new TessBaseAPI();
                                mTess.init(datapath, lang);
                            }
                        }).show();
    }
}