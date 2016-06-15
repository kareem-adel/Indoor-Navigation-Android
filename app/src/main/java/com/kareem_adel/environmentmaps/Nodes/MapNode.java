package com.kareem_adel.environmentmaps.Nodes;

import java.util.ArrayList;

/**
 * Created by Kareem-Adel on 1/14/2016.
 */
public class MapNode {
    public static final double INF = Double.POSITIVE_INFINITY;
    private String optionalDescription;
    private int x;
    private int y;
    private ArrayList<MapNode> adjNodes;
    String NodeName;
    public String NodeTag;

    public MapNode(int x, int y) {
        setX(x);
        setY(y);
        setAdjNodes(new ArrayList<MapNode>());
    }

    public String getOptionalDescription() {
        return optionalDescription;
    }

    public void setOptionalDescription(String optionalDescription) {
        this.optionalDescription = optionalDescription;
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

    public ArrayList<MapNode> getAdjNodes() {
        return adjNodes;
    }

    public void setAdjNodes(ArrayList<MapNode> adjNodes) {
        this.adjNodes = adjNodes;
    }

    public String getNodeName() {
        return NodeName;
    }

    public void setNodeName(String nodeName) {
        NodeName = nodeName;
    }

    public String getNodeTag() {
        return NodeTag;
    }

    public void setNodeTag(String nodeTag) {
        NodeTag = nodeTag;
    }

}
