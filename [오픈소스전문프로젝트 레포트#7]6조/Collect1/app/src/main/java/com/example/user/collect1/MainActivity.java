package com.example.user.collect1;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;

    private SensorManager mSensorManager = null;

    private SensorEventListener mAccLis;
    private SensorEventListener mGyroLis;
    private SensorEventListener mOriLis;

    private Sensor mAccelometerSensor = null;
    private Sensor mGyroscopeSensor = null;
    private Sensor mOrientationSensor = null;

    TextView acc_x, acc_y, acc_z;
    TextView gyro_x, gyro_y, gyro_z;
    TextView ori_x, ori_y, ori_z;

    Button btn_start;
    Button btn_stop;
    Button btn_makefile;
    Button btn_makedir;

    int filenum;
    int esti_count = 0;
    int exp_count = 100;

    Timerhandler timerHandler = null;
    sensorList sensorlist = null;
    SensorData sensortemp = null;
    String strSDpath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();


        strSDpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File myDir = new File(strSDpath + "/mydir");

        SharedPreferences sf = getSharedPreferences("sFile", MODE_PRIVATE);
        filenum = sf.getInt("text",1);

        timerHandler = new Timerhandler();
        sensorlist = new sensorList();
        for(int i = 0 ; i < 100 ; i ++){
            sensorlist.list[i] = new SensorData();
        }
        sensortemp = new SensorData();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        assert mSensorManager != null;
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mAccLis = new AccelometerListener();
        mGyroLis = new GyroscopeListener();
        mOriLis = new OrientationListener();

        acc_x = findViewById(R.id.acc_x);
        acc_y = findViewById(R.id.acc_y);
        acc_z = findViewById(R.id.acc_z);
        gyro_x = findViewById(R.id.gyro_x);
        gyro_y = findViewById(R.id.gyro_y);
        gyro_z = findViewById(R.id.gyro_z);
        ori_x = findViewById(R.id.ori_x);
        ori_y = findViewById(R.id.ori_y);
        ori_z = findViewById(R.id.ori_z);

        btn_start = findViewById(R.id.start);
        btn_stop = findViewById(R.id.stop);
        btn_makefile = findViewById(R.id.makeFile);
        btn_makedir = findViewById(R.id.makeDir);

        btn_start.setOnClickListener(mClickListener);
        btn_stop.setOnClickListener(mClickListener);
        btn_makefile.setOnClickListener(mClickListener);
        btn_makedir.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "Make Dir>> /storage/myDir", Toast.LENGTH_SHORT).show();
                myDir.mkdir();
            }
        });
    }

    public void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences sharedPreferences = getSharedPreferences("sFile",MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        int text = filenum;
        editor.putInt("text",text);

        editor.apply();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.e("LOG", "onPause()");
        stopSensing();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        stopSensing();
    }
    private class SensorData{
        String accX, accY, accZ;
        String gyroX, gyroY, gyroZ;
        String oriX, oriY, oriZ;

        SensorData(){
            this.accX = "";
            this.accY = "";
            this.accZ = "";

            this.gyroX = "";
            this.gyroY = "";
            this.gyroZ = "";

            this.oriX = "";
            this.oriY = "";
            this.oriZ = "";
        }
        void set(SensorData d){
            setAccValue(d.accX, d.accY, d.accZ);
            setGyroValue(d.gyroX, d.gyroY, d.gyroZ);
            setOriValue(d.oriX, d.oriY, d.oriZ);
        }

        void setAccValue(String acX, String acY, String acZ){
            accX = acX;
            accY = acY;
            accZ = acZ;
        }
        void setGyroValue(String gyrX, String gyrY, String gyrZ){
            gyroX = gyrX;
            gyroY = gyrY;
            gyroZ = gyrZ;
        }
        void setOriValue(String orX, String orY, String orZ){
            oriX = orX;
            oriY = orY;
            oriZ = orZ;
        }
        String get() {
            return accX + "," + accY + "," + accZ + "," +
                    gyroX + "," + gyroY + "," + gyroZ + "," +
                    oriX + "," + oriY + "," + oriZ;
        }
        public String getX(){
            return accX;
        }
        public void setX(String a){
            accX = a;
        }
    }

    private class sensorList{
        int listSize = 0;
        SensorData[] list = new SensorData[100];

        public void add(SensorData d){
            list[listSize].set(d);
            listSize++;
        }

        public int size(){
            return listSize;
        }

        public void clear(){
            listSize = 0;
        }

    }

    private class AccelometerListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            double accX = event.values[0];
            double accY = event.values[1];
            double accZ = event.values[2];

            acc_x.setText(String.valueOf(accX));
            acc_y.setText(String.valueOf(accY));
            acc_z.setText(String.valueOf(accZ));

            sensortemp.setAccValue(Double.toString(accX), Double.toString(accY), Double.toString(accZ));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    private class GyroscopeListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {

            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            gyro_x.setText(String.valueOf(gyroX));
            gyro_y.setText(String.valueOf(gyroY));
            gyro_z.setText(String.valueOf(gyroZ));

            sensortemp.setGyroValue(Double.toString(gyroX), Double.toString(gyroY), Double.toString(gyroZ));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    private class OrientationListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {

            double oriX = event.values[0];
            double oriY = event.values[1];
            double oriZ = event.values[2];

            ori_x.setText(String.valueOf(oriX));
            ori_y.setText(String.valueOf(oriY));
            ori_z.setText(String.valueOf(oriZ));

            sensortemp.setOriValue(Double.toString(oriX), Double.toString(oriY), Double.toString(oriZ));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private Button.OnClickListener mClickListener = new View.OnClickListener(){

        @SuppressLint("ShowToast")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start: {
                    startSensing();
                    timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
                    break;
                }
                case R.id.stop: {
                    timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                    onPause();
                    break;
                }
                case R.id.makeFile: {
                    makeFile();
                    Toast.makeText(MainActivity.this, "Saving Data!"+" Data"+filenum+".csv", Toast.LENGTH_SHORT);
                    filenum++;
                    break;
                }
            }
        }
    };

    private void startSensing(){
        mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mGyroLis, mGyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mOriLis, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
        Log.i("Start Estimation", "Start Estimation");
    }
    private void stopSensing(){
        mSensorManager.unregisterListener(mAccLis);
        mSensorManager.unregisterListener(mGyroLis);
        mSensorManager.unregisterListener(mOriLis);
        Log.i("Stop Estimation", "Stop Estimation");
    }

    private class Timerhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    Log.e("TimerHandler", "Timer Start");
                    esti_count = 0;
                    sensorlist.clear();
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    Log.d("SensorList Size : ", String.valueOf(sensorlist.size()));
                    break;

                case MESSAGE_TIMER_REPEAT:
                    Log.d("TimerHandler", "Timer Repeat : " + esti_count);
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 100);

                    if (esti_count == exp_count) {
                        this.removeMessages(MESSAGE_TIMER_REPEAT);
                        Log.d("List Size : ",sensorlist.size() + "");
                        onPause();
                    } else {
                        SensorData sd = sensortemp;
                        sensorlist.add(sd);
                        Log.d("saved sensing data",sensorlist.list[esti_count].get());
                        esti_count++;
                    }
                    break;

                case MESSAGE_TIMER_STOP:
                    Log.e("TimerHandler", "Timer Stop");
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    break;
            }
        }
    }

    private void makeFile() {
        Log.i("start make files", "start make files");
        File file = new File(strSDpath +"/" + "mydir" + "/Data"+filenum+".csv");
        Log.i("finish make files", "finish make files");
        try{

            FileOutputStream outFs = new FileOutputStream(file);
            String title = "accX"+","+"accY"+","+"accZ"+","+"gyroX"+","+"gyroY"+","+"gyroZ"+","+"oriAzimuth"+","+"oriPitch"+","+"oriRoll"+"\n";
            outFs.write(title.getBytes());
            for(int i = 0 ; i < sensorlist.size(); i++) {
                Log.i(String.valueOf(i), sensorlist.list[i].get());
                Log.i("accX", sensorlist.list[i].getX());
                String stemp = sensorlist.list[i].get()+"\n";
                outFs.write(stemp.getBytes());
            }
            outFs.close();

            Log.i("Success", "Success saving file");
            Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e){
            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("Fail", "Fail saving file");
            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
