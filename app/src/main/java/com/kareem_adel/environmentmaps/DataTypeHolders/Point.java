package com.kareem_adel.environmentmaps.DataTypeHolders;

/**
 * Created by Kareem-Adel on 3/11/2016.
 */
public class Point {

    public Point(int x, int y) {
        setX(x);
        setY(y);
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

    int x;
    int y;
}
