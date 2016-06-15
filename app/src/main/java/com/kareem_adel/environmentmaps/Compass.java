package com.kareem_adel.environmentmaps;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Kareem-Adel on 1/24/2016.
 */
public class Compass {

    float azimuth;
    float preAzimuth;
    IndoorMap IndoorMap;

    Compass(Context context, IndoorMap IndoorMap) {
        this.IndoorMap = IndoorMap;

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        CompassSensorEventListener compassSensorEventListener = new CompassSensorEventListener();

        sensorManager.registerListener(compassSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(compassSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
    }


    public class CompassSensorEventListener implements SensorEventListener {

        float[] GRAVITY = new float[3];
        float[] GEOMAGNETIC = new float[3];

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_GRAVITY: {
                    GRAVITY = sensorEvent.values;
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD: {
                    GEOMAGNETIC = sensorEvent.values;
                    break;
                }
            }

            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, GRAVITY, GEOMAGNETIC)) {
                float[] ORIENTATION = new float[3];
                SensorManager.getOrientation(R, ORIENTATION);
                azimuth = (float) Math.toDegrees(ORIENTATION[0]);
                azimuth = (azimuth + 360) % 360;
                if (Math.abs(preAzimuth - azimuth) > 10) {
                    UpdateArrow();
                    preAzimuth = azimuth;
                }
            }
        }

        public void UpdateArrow() {
            float ang = 0;
            ang = ((azimuth + 180) % 360);
            if (Navigator.activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
                ang += 90;
            }
            IndoorMap.rAng = ang;
            IndoorMap.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


    }

}
