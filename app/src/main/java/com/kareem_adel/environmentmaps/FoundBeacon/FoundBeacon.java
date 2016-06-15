package com.kareem_adel.environmentmaps.FoundBeacon;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

/**
 * Created by Kareem-Adel on 1/25/2016.
 */
public class FoundBeacon {

    public short getRSSI() {
        return RSSI;
    }

    public void setRSSI(short RSSI) {
        this.RSSI = RSSI;
    }

    public BluetoothDevice getDevice() {
        return Device;
    }

    public void setDevice(BluetoothDevice device) {
        Device = device;
    }

    public long getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(long creationDate) {
        CreationDate = creationDate;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }


    private BluetoothDevice Device;
    private long CreationDate;
    private double temperature;
    private int x;
    private int y;
    short RSSI;
    double Distance;


    double AccumulatedAccuracyInverse = 0;
    int NumberOfDistanceSamples = 0;


    public FoundBeacon(BluetoothDevice bluetoothDevice, short RSSI, double temperature) {
        InitFoundBeacon(bluetoothDevice, RSSI, temperature);
    }

    public void InitFoundBeacon(BluetoothDevice bluetoothDevice, short RSSI, double temperature) {
        this.Device = bluetoothDevice;
        this.RSSI = RSSI;
        this.Distance = calculateAccuracy(-85);
        this.CreationDate = new Date().getTime();
        this.temperature = temperature;
        x = getLocation()[0];
        y = getLocation()[1];
    }

    public void AccumulateReadings() {
        this.AccumulatedAccuracyInverse += calculateAccuracy(-65);
        NumberOfDistanceSamples++;
    }



    public double GetAverageAccuracyInverse() {
        double ret;
        if (NumberOfDistanceSamples == 0) {
            ret = 0;
        } else {
            ret = AccumulatedAccuracyInverse / NumberOfDistanceSamples;
        }

        AccumulatedAccuracyInverse = 0;
        NumberOfDistanceSamples = 0;
        return ret;
    }

    public int[] getLocation() {
        int[] Location = new int[2];
        String[] sLocation = Device.getName().replace("An", "").split("s");
        Location[0] = Integer.parseInt(sLocation[0]);
        Location[1] = Integer.parseInt(sLocation[1]);
        return Location;
    }

    public double calculateAccuracy(double txPower) {
        if (getRSSI() == 0) {
            return -1.0;
        }
        double ratio = getRSSI() * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            return ((0.89976) * Math.pow(ratio, 7.7095) + 0.111);
        }
    }

    public double getAccumulatedAccuracyInverse() {
        return AccumulatedAccuracyInverse;
    }

    public void setAccumulatedAccuracyInverse(double accumulatedAccuracyInverse) {
        AccumulatedAccuracyInverse = accumulatedAccuracyInverse;
    }

    public int getNumberOfDistanceSamples() {
        return NumberOfDistanceSamples;
    }

    public void setNumberOfDistanceSamples(int numberOfDistanceSamples) {
        NumberOfDistanceSamples = numberOfDistanceSamples;
    }
}
