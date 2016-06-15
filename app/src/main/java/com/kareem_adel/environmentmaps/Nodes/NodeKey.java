package com.kareem_adel.environmentmaps.Nodes;

/**
 * Created by Kareem-Adel on 1/14/2016.
 */
public class NodeKey {
    int x;
    int y;

    public NodeKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return (x == ((NodeKey) o).x) && (y == ((NodeKey) o).y);
    }

    @Override
    public int hashCode() {
        return 51 * x + y;
    }
}
