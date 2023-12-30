package com.example.sensorcheck3;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView myText_;
    private TextView message_;

    // SensorManagerインスタンス
    private SensorManager sma_;
    private Handler handler_;

    RingBuffer<SensorEvent> ringBuffer_ = new RingBuffer<>(60);

    SensorEvent event_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myText_ = findViewById(R.id.my_text);
        message_ = findViewById(R.id.message);
        message_.setText("ニュートラル");

        // SensorManagerのインスタンスを取得する
        sma_ = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sma_.registerListener(this, sma_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        handler_ = new Handler(Looper.getMainLooper());

        // メインループを開始
        startMainLoop();

        // ジェスチャーの評価関数の登録
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                evaluate();
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    // センサーの評価
    private void evaluate()
    {
        if(!ringBuffer_.isFull())
        {
            System.out.println("buffer is not full");
            return;
        }

        // 評価する
        float x = 0f; // +左に傾き -右に傾き (最大10)
        float avgX = 0f;

        for(int i = 0; i < ringBuffer_.size(); i++){
            SensorEvent event = ringBuffer_.get(i);
            x += event.values[0];
        }
        avgX = x / ringBuffer_.size();
        System.out.println(String.format("avgX %f", avgX));

        String message = "傾いてません";

        // 判定する
        if (avgX > 3.0f) {
            // 左に傾いている
            message = "左に傾いています";
        } else if (avgX < -3.0f) {
            // 右に傾いている
            message = "右に傾いています";
        }

        message_.setText(message);
    }

    private void startMainLoop() {
        handler_.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 現在のセンサーイベントをリングバッファに格納
                ringBuffer_.add(event_);

                // メインループを再度呼び出す
                startMainLoop();
            }
        }, 1000 / 60);
    }

    // センサーの値が変化すると呼ばれる
    public void onSensorChanged(SensorEvent event) {

        // 現在のイベント
        event_ = event;

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        // 四捨五入
        x = ((double)Math.round(x * 10))/10;
        y = ((double)Math.round(y * 10))/10;
        z = ((double)Math.round(z * 10))/10;

        String str =   "加速度センサー\n X= " + x + "\n"
                              + " Y= " + y + "\n"
                              + " Z= " + z;
        myText_.setText(str);

    }

    // センサーの精度が変更されると呼ばれる
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
