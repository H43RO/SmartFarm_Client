package sch.iot.onem2mapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CctvActivity extends AppCompatActivity implements Button.OnClickListener {
    private String src;
    private TextView led_on_off;
    private CardView capture_card;
    private CardView report_card;
    private CardView setting_card;
    private CardView turn_left;
    private CardView turn_right;

    private WebView webView;

    //화면 캡쳐하기
    public File ScreenShot(View view){
        view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용
        Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        String now = date.format(new Date()) + "_" + time.format(new Date());
        String filename = "farm_" + now + ".jpg";

        File file = new File(Environment.getExternalStorageDirectory()+"/CCTV");

        if(!file.exists()){ //폴더가 존재하지 않으면 생성
            file.mkdir();
        }

        file = new File(Environment.getExternalStorageDirectory()+"/CCTV", filename); //세부 파일명 생성
        FileOutputStream os;

        try{
            os = new FileOutputStream(file); //'file'에 쓰기동작을 할 FileOutputStream 초기화
            screenBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);   //비트맵을 JPG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        view.setDrawingCacheEnabled(false);
        return file;
    }

    @Override
    public void onClick(View v) {
        led_on_off = findViewById(R.id.led_on_off_text);

        switch (v.getId()){
            case R.id.ledOnButton:
                if(((ToggleButton) v).isChecked()){
                    led_on_off.setText("LED ON");
                    MainActivity.ControlRequest On = new MainActivity.ControlRequest("10");
                    On.setReceiver(new MainActivity.IReceived() {
                        @Override
                        public void getResponseBody(String msg) {

                        }
                    });
                    On.start();
                }else{
                    led_on_off.setText("LED OFF");
                    MainActivity.ControlRequest Off = new MainActivity.ControlRequest("0");
                    Off.setReceiver(new MainActivity.IReceived() {
                        @Override
                        public void getResponseBody(String msg) {

                        }
                    });
                    Off.start();
                }
                break;

            case R.id.turn_left:
                MainActivity.ControlRequestServo left = new MainActivity.ControlRequestServo("21");
                left.setReceiver(new MainActivity.IReceived() {
                    @Override
                    public void getResponseBody(String msg) {

                    }
                });
                left.start();
                break;

            case R.id.turn_right:
                MainActivity.ControlRequestServo right = new MainActivity.ControlRequestServo("20");
                right.setReceiver(new MainActivity.IReceived() {
                    @Override
                    public void getResponseBody(String msg) {

                    }
                });
                right.start();
                break;

            case R.id.capture_card:
                //파일 쓰기 권한 취득 확인
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("screen_test","권한 부여 안됨");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1004);
                }else{
                    File screenShot = ScreenShot(webView);
                    if(screenShot!=null){
                        //갤러리에 추가
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                        Toast.makeText(this,"CCTV 캡쳐되었습니다!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this,"캡쳐 실패", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case R.id.report_card:
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                startActivity(call);
                break;

            case R.id.setting_card:
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    public ToggleButton led_toggle;

    @Override
    protected void onResume() {
        super.onResume();
        src = MainActivity.info.rpi_address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);

        led_toggle = findViewById(R.id.ledOnButton);
        led_toggle.setOnClickListener(this);

        capture_card =findViewById(R.id.capture_card);
        capture_card.setOnClickListener(this);

        report_card = findViewById(R.id.report_card);
        report_card.setOnClickListener(this);

        setting_card = findViewById(R.id.setting_card);
        setting_card.setOnClickListener(this);

        turn_left = findViewById(R.id.turn_left);
        turn_left.setOnClickListener(this);
        turn_right = findViewById(R.id.turn_right);
        turn_right.setOnClickListener(this);

        WebSettings webSettings;

        webView = (WebView)findViewById(R.id.cctv);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        src = MainActivity.info.rpi_address;

        webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} " +
                        "img{width:100%25;} div{overflow: hidden;} </style></head>" +
                        "<body><div><img src='"+ src +"'/></div></body></html>" ,
                "text/html",  "UTF-8");

    }
}