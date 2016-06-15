package com.kareem_adel.environmentmaps;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.kareem_adel.environmentmaps.FoundBeacon.FoundBeacon;
import com.kareem_adel.environmentmaps.FoundBeacon.FoundBeaconWithAverageDistance;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class BlueCloudService extends Service {
    // Navigation (Voice / notifications)
    public BluetoothAdapter.LeScanCallback mLeScanCallback;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    public BluetoothAdapter mBluetoothAdapter = null;

    public Timer mTimer;
    public Timer mTimerGetLocation;
    public Hashtable<String, FoundBeacon> FoundBeacons = new Hashtable<String, FoundBeacon>();

    public static int currentLocationX = 10;
    public static int currentLocationY = 10;


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            Intent intent = bundle.getParcelable("intent");

            if (intent == null)
                return;

            final ResultReceiver resRec = intent.getParcelableExtra("resReceiver");
            int Command = intent.getIntExtra("Command", -1);

            final Bundle resultBundle = new Bundle();

            /*
            switch (Command) {
                case cmd: {

                    break;
                }

            }
            */

            //stopSelf(msg.arg1);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent", intent);
        msg.setData(bundle);
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Thread.MIN_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    private void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    String Name = device.getName();
                    if (Name != null && Name.matches("(\\d+)s(\\d+)")) {
                        //parse heat and humidity and use method below to insert data
                        double temp = -1;
                        for (int i = 0; i < scanRecord.length; i++) {
                            if (i < scanRecord.length - 7) {
                                if (scanRecord[i] == 7 && scanRecord[i + 1] == 22 && scanRecord[i + 2] == 0 && scanRecord[i + 3] == -80) {
                                    if (scanRecord[i + 5] > 0)
                                        temp = scanRecord[i + 5];
                                }
                            }
                        }
                        FoundBeacon foundBeacon = FoundBeacons.get(device.getAddress());
                        if (foundBeacon != null) {
                            foundBeacon.InitFoundBeacon(device, (short) rssi, temp);
                        } else {
                            foundBeacon = new FoundBeacon(device, (short) rssi, temp);
                            FoundBeacons.put(device.getAddress(), foundBeacon);
                        }
                        foundBeacon.AccumulateReadings();
                    }

                }
            };
        }
        mTimer = new Timer();
        mTimerGetLocation = new Timer();

        startBTListeningAndLocalizationInterval();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doDiscovery() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private void cancelDiscovery() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) -32768);
                if (device.fetchUuidsWithSdp()) {
                    String Name = device.getName();
                    if (Name != null && Name.matches("(\\d+)s(\\d+)")) {
                        FoundBeacon foundBeacon = FoundBeacons.get(device.getAddress());
                        if (foundBeacon != null) {
                            foundBeacon.InitFoundBeacon(device, (short) RSSI, -1);
                        } else {
                            foundBeacon = new FoundBeacon(device, (short) RSSI, -1);
                            FoundBeacons.put(device.getAddress(), foundBeacon);
                        }
                        foundBeacon.AccumulateReadings();

                    }
                }
            }
        }

    };

    public void startBTListeningAndLocalizationInterval() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mTimerGetLocation.scheduleAtFixedRate(GetLocationTask, 0, 1500);
        } else {
            mTimer.scheduleAtFixedRate(ScannerTask, 0, 10000);
        }
    }

    TimerTask ScannerTask = new TimerTask() {
        @Override
        public void run() {

            cancelDiscovery();

            if (Navigator.activity != null && Navigator.activity != null && Navigator.activity.indoorMap != null) {
                Navigator.activity.indoorMap.tx = BlueCloudService.currentLocationX;
                Navigator.activity.indoorMap.ty = BlueCloudService.currentLocationY;
                Navigator.activity.indoorMap.PrevLocationX = BlueCloudService.currentLocationX;
                Navigator.activity.indoorMap.PrevLocationY = BlueCloudService.currentLocationY;
            }
            //operations on scanned devices
            //final Point ApproximateCircle = GetApproximatePosition();
            final Point ApproximateCircle = getLocation();
            if (ApproximateCircle != null) {
                currentLocationX = ApproximateCircle.x;
                currentLocationY = ApproximateCircle.y;
            }
            if (Navigator.activity != null && Navigator.activity != null && Navigator.activity.indoorMap != null) {
                Navigator.activity.indoorMap.StartTime = System.currentTimeMillis();
                Navigator.activity.indoorMap.postInvalidate();
            }
            doDiscovery();
        }
    };

    TimerTask GetLocationTask = new TimerTask() {
        @Override
        public void run() {

            if (Navigator.activity != null && Navigator.activity != null && Navigator.activity.indoorMap != null) {
                Navigator.activity.indoorMap.tx = BlueCloudService.currentLocationX;
                Navigator.activity.indoorMap.ty = BlueCloudService.currentLocationY;
                Navigator.activity.indoorMap.PrevLocationX = BlueCloudService.currentLocationX;
                Navigator.activity.indoorMap.PrevLocationY = BlueCloudService.currentLocationY;
            }
            //final Point ApproximateCircle = GetApproximatePosition();
            final Point ApproximateCircle = getLocation();
            if (ApproximateCircle != null) {
                currentLocationX = ApproximateCircle.x;
                currentLocationY = ApproximateCircle.y;

            }
            if (Navigator.activity != null && Navigator.activity != null && Navigator.activity.indoorMap != null) {
                Navigator.activity.indoorMap.StartTime = System.currentTimeMillis();
                Navigator.activity.indoorMap.postInvalidate();
            }
        }
    };

    public Point getLocation() {
        ArrayList<FoundBeaconWithAverageDistance> foundBeaconsWithDistances = new ArrayList<>();
        double TotalDistance = 0;
        Enumeration<FoundBeacon> foundBeaconEnumeration = FoundBeacons.elements();
        long MostRecent = 0;
        //get the most recent beacon date AND get all the beacons average distances at the same time
        while (foundBeaconEnumeration.hasMoreElements()) {
            FoundBeacon foundBeacon = foundBeaconEnumeration.nextElement();
            if (foundBeacon.getCreationDate() > MostRecent) {
                MostRecent = foundBeacon.getCreationDate();
            }
            FoundBeaconWithAverageDistance foundBeaconWithAverageDistance = new FoundBeaconWithAverageDistance(foundBeacon);
            foundBeaconsWithDistances.add(foundBeaconWithAverageDistance);
            TotalDistance += foundBeaconWithAverageDistance.getAverageAccuracyInverse();
        }
        if (TotalDistance == 0) {
            return new Point(currentLocationX, currentLocationY);
        }
        double sumXRatio = 0, sumYRatio = 0;
        for (FoundBeaconWithAverageDistance foundBeaconWithAverageDistance : foundBeaconsWithDistances) {
            if (foundBeaconWithAverageDistance.getFoundBeacon().getCreationDate() - MostRecent > 1 * 1000) {
                continue;
            }
            sumXRatio += (foundBeaconWithAverageDistance.getAverageAccuracyInverse() / TotalDistance) * foundBeaconWithAverageDistance.getFoundBeacon().getX();
            sumYRatio += (foundBeaconWithAverageDistance.getAverageAccuracyInverse() / TotalDistance) * foundBeaconWithAverageDistance.getFoundBeacon().getY();
        }
        if (Math.sqrt(Math.pow(currentLocationX - (int) sumXRatio, 2) + Math.pow(currentLocationY - (int) sumYRatio, 2)) < 8) {
            return new Point(currentLocationX, currentLocationY);
        } else {
            return new Point((int) sumXRatio, (int) sumYRatio);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }
}
