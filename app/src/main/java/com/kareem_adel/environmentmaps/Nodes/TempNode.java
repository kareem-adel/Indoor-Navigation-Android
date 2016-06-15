package com.kareem_adel.environmentmaps.Nodes;

/**
 * Created by Kareem-Adel on 3/27/2016.
 */
public class TempNode {
    double temp;
    double transparencyFactor;
    private boolean isPrimary;

    public double getTransparencyFactor() {
        return transparencyFactor;
    }

    public void setTransparencyFactor(double transparencyFactor) {
        this.transparencyFactor = transparencyFactor;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
