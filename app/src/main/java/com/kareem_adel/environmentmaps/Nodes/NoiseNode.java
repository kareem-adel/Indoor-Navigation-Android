package com.kareem_adel.environmentmaps.Nodes;

/**
 * Created by Kareem-Adel on 3/27/2016.
 */
public class NoiseNode {
    double noise;
    double transparencyFactor;
    private boolean isPrimary;

    public double getTransparencyFactor() {
        return transparencyFactor;
    }

    public void setTransparencyFactor(double transparencyFactor) {
        this.transparencyFactor = transparencyFactor;
    }
    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
