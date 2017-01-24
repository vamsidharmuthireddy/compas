package com.example.home.compas;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity implements SensorEventListener {

    Float azimuth;  // View to draw a compass
    Float pitch;
    Float roll;

    public class CustomDrawableView extends View {
        Paint paint = new Paint();
        public CustomDrawableView(Context context) {
            super(context);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setAntiAlias(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //keep screen turned on
        }

        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int centerx = width / 2;
            int centery = height / 2;
            canvas.drawLine(centerx, 0, centerx, height, paint);
            canvas.drawLine(0, centery, width, centery, paint);

            paint.setStrokeWidth(5);
            // Rotate the canvas with the azimuth
            if (azimuth != null) {

                String azimuthDisplay = "azimuth = " + azimuth;
                String pitchDisplay = "pitch = " + pitch;
                String rollDisplay = "roll = " + roll;

                canvas.drawText(azimuthDisplay, 0, height - 300, paint);
                canvas.drawText(pitchDisplay, 0, height - 200, paint);
                canvas.drawText(rollDisplay, 0, height - 100, paint);


                canvas.rotate(-azimuth, centerx, centery);
            }
            paint.setColor(0xff0000ff);
            paint.setTextSize(50);
            canvas.drawLine(centerx, 0, centerx, 2*centery, paint);
            canvas.drawLine(0, centery, 2*centerx , centery, paint);
            canvas.drawText("N", centerx-18, centery-50, paint);
            canvas.drawText("S", centerx-18, centery+90, paint);
            paint.setColor(Color.RED);
            canvas.save();
            canvas.restore();


        }
    }

    CustomDrawableView mCustomDrawableView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomDrawableView = new CustomDrawableView(this);
        setContentView(mCustomDrawableView);    // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // at this point, orientation contains the azimuth(direction), pitch and roll values.
                azimuth = (float) (180 * orientation[0] / Math.PI);
                pitch = (float)(180 * orientation[1] / Math.PI);
                roll = (float) (180 * orientation[2] / Math.PI);

            }
        }



        mCustomDrawableView.invalidate();

    }
}