package com.kareem_adel.environmentmaps.FoundBeacon;

/**
 * Created by Kareem-Adel on 3/21/2016.
 */
public class FoundBeaconWithAverageDistance {
    public FoundBeaconWithAverageDistance(FoundBeacon foundBeacon) {
        this.foundBeacon = foundBeacon;
        this.averageAccuracyInverse = foundBeacon.GetAverageAccuracyInverse();
    }

    public FoundBeacon getFoundBeacon() {
        return foundBeacon;
    }

    public void setFoundBeacon(FoundBeacon foundBeacon) {
        this.foundBeacon = foundBeacon;
    }

    public double getAverageAccuracyInverse() {
        return averageAccuracyInverse;
    }

    public void setAverageAccuracyInverse(double averageAccuracyInverse) {
        this.averageAccuracyInverse = averageAccuracyInverse;
    }

    FoundBeacon foundBeacon;
    double averageAccuracyInverse;
}
