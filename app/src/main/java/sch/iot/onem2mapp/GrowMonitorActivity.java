package sch.iot.onem2mapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/* File Format로 & Logic
    - 날짜별로 1회 촬영
    - YYYYMMDD.jpg 로 저장 ex) 20200616.jpg
    - Android 단에서 해당 날짜 파일 존재하지 않을 시 동기화 함 (FTP Client 통해서 사진 다운드)
    - 파일명 (날짜순)으로 정렬하여 ArrayList<String>에 파일 경로를 넣어서 RecyclerView Adpater 연결
 */

public class GrowMonitorActivity extends AppCompatActivity {
    private ConnectFTP ConnectFTP = new ConnectFTP();
    final String TAG = "Activity FTP";
    String currentPath;
    ImageView imageView;
    ImageView imageView2;

    public ArrayList<Bitmap> photos;
    String strImage;

    String newFilePath = Environment.getExternalStorageDirectory() + "/GrowUpData";
    File file = new File(newFilePath);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grow_monitor);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        //파일 다운로더 클래스
        DownloadFileTask download = new DownloadFileTask();

        //오늘의 파일 명 구하기
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        String now = date.format(new Date());
        String today_filename = now + ".jpg";
        File todayFile = new File(Environment.getExternalStorageDirectory() + "/GrowUpData/", today_filename);
        if (!todayFile.exists()) {
            download.execute();
        } else {
            Log.d("file_sync", "파일이 이미 있습니다");
        }

        try {
            String newFilePath = Environment.getExternalStorageDirectory() + "/GrowUpData/raspi5.jpg";
            File file2 = new File(newFilePath);

            if (file2.exists()) {
                Bitmap bitmap2 = BitmapFactory.decodeFile(file2.getAbsolutePath());
                Log.d("test_img_saving", file2.getAbsolutePath());
                imageView2.setImageBitmap(bitmap2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            boolean status = false;
            String host = "192.168.0.246";
            String username = "pi";
            String password = "rlaguswns5";
            status = ConnectFTP.ftpConnect(host, username, password, 21);

            if (status == true) {
                Log.d(TAG, "Connection 성공");
            } else {
                Log.d(TAG, "Connection 실패");
            }

            SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            String now = date.format(new Date());
            String today_filename = now + ".jpg";

            currentPath = ConnectFTP.ftpGetDirectory();
            newFilePath += "/" + today_filename;

            try {
                if (!file.exists()) {
                    file.mkdir();
                }
                file = new File(newFilePath);
                file.createNewFile();
            } catch (Exception e) {
                Log.d(TAG, "실패");
            }

            ConnectFTP.ftpDownloadFile(currentPath + "/Pictures/" + today_filename, newFilePath);
            ConnectFTP.ftpDisconnect(); //CCTV Activity에서 RTSP 통신해줘야하기 때문에 FTP Close함

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(getApplicationContext(), "동기화 성공", Toast.LENGTH_LONG).show();

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);


        }
    }

}