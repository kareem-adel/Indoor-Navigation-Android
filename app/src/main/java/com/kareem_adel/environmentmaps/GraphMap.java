package com.kareem_adel.environmentmaps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.kareem_adel.environmentmaps.Nodes.MapNode;
import com.kareem_adel.environmentmaps.Nodes.NodeKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by Kareem-Adel on 1/30/2016.
 */
class GraphMap {

    static MapNode[] mapNodes;
    //static boolean[] mapNodeWalkable;

    static Hashtable<NodeKey, MapNode> nodeKeyMapNodeHashtable;
    static Bitmap indoorMapImage;
    static Bitmap PinImage;
    static Bitmap ArrowImage;

    static int WallMarginRadius = 5;
    static int NodesMargin = 4;

    public static void InitSlowGraph() {

        InitFastGraph();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        if (indoorMapImage == null)
            GraphMap.setIndoorMapImage(BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.hannovermessev3, opts));
        if (PinImage == null)
            PinImage = BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.ic_pin_smaller, opts);
        if (ArrowImage == null)
            ArrowImage = BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.arrowv2, opts);

        if (mapNodes == null) {
            mapNodes = new MapNode[getImageSize()];
            //mapNodeWalkable = new boolean[getImageSize()];

            int[] pixels = new int[getImageSize()];
            indoorMapImage.getPixels(pixels, 0, indoorMapImage.getWidth(), 0, 0, indoorMapImage.getWidth(), indoorMapImage.getHeight());
            for (int x = 0; x < indoorMapImage.getWidth() - 1; x++) {
                for (int y = 0; y < indoorMapImage.getHeight() - 1; y++) {
                    if (x % NodesMargin == 0 && y % NodesMargin == 0 && (pixels[getIndex(x, y)] == Color.argb(255, 224, 224, 224) || pixels[getIndex(x, y)] == Color.argb(255, 1, 1, 255))) {
                        mapNodes[getIndex(x, y)] = new MapNode(x, y);
                    }
                }
            }

            for (int x = 0; x < indoorMapImage.getWidth() - 1; x++) {
                for (int y = 0; y < indoorMapImage.getHeight() - 1; y++) {
                    if (pixels[getIndex(x, y)] == Color.argb(255, 84, 84, 84)) {
                        createMarginRadius(x, y);
                    }
                }
            }

            for (int x = 0; x < indoorMapImage.getWidth() - 1; x++) {
                for (int y = 0; y < indoorMapImage.getHeight() - 1; y++) {
                    if (getIndex(x, y) != -1 && mapNodes[getIndex(x, y)] != null) {
                        final int finalY = y;
                        final int finalX = x;
                        mapNodes[getIndex(x, y)].setAdjNodes(new ArrayList<MapNode>() {
                            {
                                if (getIndex(finalX - NodesMargin, finalY) != -1 && mapNodes[getIndex(finalX - NodesMargin, finalY)] != null) {
                                    add(mapNodes[getIndex(finalX - NodesMargin, finalY)]);
                                }
                                if (getIndex(finalX - NodesMargin, finalY - NodesMargin) != -1 && mapNodes[getIndex(finalX - NodesMargin, finalY - NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX - NodesMargin, finalY - NodesMargin)]);
                                }
                                if (getIndex(finalX, finalY - NodesMargin) != -1 && mapNodes[getIndex(finalX, finalY - NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX, finalY - NodesMargin)]);
                                }
                                if (getIndex(finalX + NodesMargin, finalY - NodesMargin) != -1 && mapNodes[getIndex(finalX + NodesMargin, finalY - NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX + NodesMargin, finalY - NodesMargin)]);
                                }
                                if (getIndex(finalX + NodesMargin, finalY) != -1 && mapNodes[getIndex(finalX + NodesMargin, finalY)] != null) {
                                    add(mapNodes[getIndex(finalX + NodesMargin, finalY)]);
                                }
                                if (getIndex(finalX + NodesMargin, finalY + NodesMargin) != -1 && mapNodes[getIndex(finalX + NodesMargin, finalY + NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX + NodesMargin, finalY + NodesMargin)]);
                                }
                                if (getIndex(finalX, finalY + NodesMargin) != -1 && mapNodes[getIndex(finalX, finalY + NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX, finalY + NodesMargin)]);
                                }
                                if (getIndex(finalX - NodesMargin, finalY + NodesMargin) != -1 && mapNodes[getIndex(finalX - NodesMargin, finalY + NodesMargin)] != null) {
                                    add(mapNodes[getIndex(finalX - NodesMargin, finalY + NodesMargin)]);
                                }
                            }
                        });
                    }
                }
            }
        }
    }


    public static void createMarginRadius(int x, int y) {
        for (int a = (x - (WallMarginRadius / 2)); a < x + (WallMarginRadius / 2); a++) {
            for (int b = (y - (WallMarginRadius / 2)); b < y + (WallMarginRadius / 2); b++) {
                if (getIndex(a, b) != -1 && Math.pow((x - a), 2) + Math.pow((y - b), 2) < Math.pow(WallMarginRadius, 2))
                    mapNodes[getIndex(a, b)] = null;
            }
        }
    }

    public static void InitFastGraph() {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        if (indoorMapImage == null)
            GraphMap.setIndoorMapImage(BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.hannovermessev3, opts));
        if (PinImage == null)
            PinImage = BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.ic_pin_smaller, opts);
        if (ArrowImage == null)
            ArrowImage = BitmapFactory.decodeResource(Navigator.activity.getResources(), R.drawable.arrowv2, opts);

        if (nodeKeyMapNodeHashtable == null) {
            nodeKeyMapNodeHashtable = new Hashtable<NodeKey, MapNode>();

            String MapData = "146,444-213,443-80,445-link=NA\n" +
                    "822,444-806,445-820,350-891,441-NA=NA\n" +
                    "85,114-179,113-86,172-NA=NA\n" +
                    "88,288-88,230-88,346-link=NA\n" +
                    "765,230-818,230-712,230-link=NA\n" +
                    "756,404-756,459-757,349-link=NA\n" +
                    "806,445-803,510-822,444-NA=NA\n" +
                    "888,232-890,174-888,289-NA=NA\n" +
                    "803,510-756,515-806,445-NA=NA\n" +
                    "891,441-822,444-888,347-NA=NA\n" +
                    "756,515-691,513-803,510-756,459-NA=NA\n" +
                    "345,348-344,289-417,349-332,348-NA=NA\n" +
                    "435,229-434,287-473,228-390,230-NA=NA\n" +
                    "416,404-416,459-417,349-link=NA\n" +
                    "86,172-88,230-85,114-link=NA\n" +
                    "180,348-217,348-88,346-178,289-NA=NA\n" +
                    "240,113-179,113-302,113-link=NA\n" +
                    "890,174-893,116-888,232-link=NA\n" +
                    "554,288-557,229-552,347-link=NA\n" +
                    "416,514-335,517-489,512-416,459-NA=NA\n" +
                    "417,349-345,348-433,346-416,404-NA=NA\n" +
                    "334,460-333,404-335,517-link=NA\n" +
                    "711,289-712,230-711,348-link=NA\n" +
                    "489,512-563,511-416,514-link=NA\n" +
                    "537,171-535,114-540,228-link=NA\n" +
                    "563,456-564,402-563,511-link=NA\n" +
                    "475,114-390,114-535,114-474,171-NA=NA\n" +
                    "756,459-756,404-756,515-link=NA\n" +
                    "278,346-276,405-266,345-332,348-NA=NA\n" +
                    "302,113-240,113-345,111-301,171-NA=NA\n" +
                    "888,289-888,232-888,347-link=NA\n" +
                    "618,230-557,229-634,231-619,172-NA=NA\n" +
                    "633,289-634,231-633,347-link=NA\n" +
                    "213,443-146,444-213,463-217,348-NA=NA\n" +
                    "691,513-627,512-756,515-link=NA\n" +
                    "390,114-345,111-475,114-390,172-NA=NA\n" +
                    "276,405-278,346-275,464-link=NA\n" +
                    "266,229-301,230-176,230-266,287-NA=NA\n" +
                    "266,345-278,346-217,348-266,287-NA=NA\n" +
                    "474,171-473,228-475,114-link=NA\n" +
                    "390,172-390,230-390,114-link=NA\n" +
                    "712,230-765,230-634,231-711,289-NA=NA\n" +
                    "791,113-734,113-842,114-NA=NA\n" +
                    "677,113-734,113-620,114-link=NA\n" +
                    "563,511-489,512-627,512-563,456-NA=NA\n" +
                    "214,519-187,498-271,521-NA=NA\n" +
                    "634,231-618,230-712,230-633,289-NA=NA\n" +
                    "345,111-302,113-390,114-NA=NA\n" +
                    "627,512-691,513-563,511-link=NA\n" +
                    "552,347-565,348-554,288-492,346-NA=NA\n" +
                    "333,404-334,460-332,348-link=NA\n" +
                    "492,346-552,347-433,346-link=NA\n" +
                    "301,171-301,230-302,113-link=NA\n" +
                    "187,498-213,463-214,519-NA=NA\n" +
                    "332,348-345,348-278,346-333,404-NA=NA\n" +
                    "271,521-214,519-335,517-275,464-NA=NA\n" +
                    "434,287-433,346-435,229-link=NA\n" +
                    "301,230-343,231-301,171-266,229-NA=NA\n" +
                    "88,346-88,288-80,445-180,348-NA=NA\n" +
                    "433,346-492,346-434,287-417,349-NA=NA\n" +
                    "178,289-180,348-176,230-link=NA\n" +
                    "619,172-620,114-618,230-link=NA\n" +
                    "275,464-213,463-276,405-271,521-NA=NA\n" +
                    "473,228-435,229-540,228-474,171-NA=NA\n" +
                    "535,114-475,114-537,171-NA=NA\n" +
                    "711,348-757,349-633,347-711,289-NA=NA\n" +
                    "819,290-818,230-820,350-link=NA\n" +
                    "888,347-888,289-820,350-891,441-NA=NA\n" +
                    "176,230-266,229-177,171-88,230-178,289-NA=NA\n" +
                    "344,289-343,231-345,348-link=NA\n" +
                    "620,114-619,172-677,113-NA=NA\n" +
                    "564,402-563,456-565,348-link=NA\n" +
                    "80,445-88,346-146,444-NA=NA\n" +
                    "343,231-390,230-301,230-344,289-NA=NA\n" +
                    "335,517-271,521-416,514-334,460-NA=NA\n" +
                    "818,230-819,290-765,230-NA=NA\n" +
                    "820,350-819,290-888,347-757,349-822,444-NA=NA\n" +
                    "213,463-213,443-187,498-275,464-NA=NA\n" +
                    "217,348-266,345-180,348-213,443-NA=NA\n" +
                    "565,348-633,347-552,347-564,402-NA=NA\n" +
                    "633,347-711,348-565,348-633,289-NA=NA\n" +
                    "842,114-791,113-893,116-link=NA\n" +
                    "416,459-416,404-416,514-link=NA\n" +
                    "177,171-176,230-179,113-link=NA\n" +
                    "390,230-435,229-390,172-343,231-NA=NA\n" +
                    "179,113-85,114-240,113-177,171-NA=NA\n" +
                    "266,287-266,345-266,229-link=NA\n" +
                    "88,230-176,230-86,172-88,288-NA=NA\n" +
                    "893,116-842,114-890,174-NA=NA\n" +
                    "734,113-677,113-791,113-link=NA\n" +
                    "757,349-820,350-711,348-756,404-NA=NA\n" +
                    "540,228-537,171-557,229-473,228-NA=NA\n" +
                    "557,229-540,228-618,230-554,288-NA=NA";

            String[] MapNodesWithAdj = MapData.split("\n");

            for (int i = 0; i < MapNodesWithAdj.length; i++) {
                String[] NodeXY;
                String[] NameTag;

                String[] NodeAdjNameTag = MapNodesWithAdj[i].split("-");
                NodeXY = NodeAdjNameTag[0].split(",");
                NameTag = NodeAdjNameTag[NodeAdjNameTag.length - 1].split("=");

                int x = Integer.parseInt(NodeXY[0]);
                int y = Integer.parseInt(NodeXY[1]);
                InsertNewMapNode(x, y);
                getMapNode(x, y).setNodeName(NameTag[0]);
                getMapNode(x, y).setNodeTag(NameTag[1]);

            }
            for (int i = 0; i < MapNodesWithAdj.length; i++) {
                if (MapNodesWithAdj[i].split("-").length > 2) {
                    String[] NodeWithAdjs = MapNodesWithAdj[i].split("-");
                    String[] NodeXY = NodeWithAdjs[0].split(",");
                    ArrayList<MapNode> mapNodesAdj = new ArrayList<>();
                    for (int j = 1; j < NodeWithAdjs.length - 1; j++) {
                        String[] AdjNodeXY = NodeWithAdjs[j].split(",");
                        mapNodesAdj.add(getMapNode(Integer.parseInt(AdjNodeXY[0]), Integer.parseInt(AdjNodeXY[1])));
                    }
                    getMapNode(Integer.parseInt(NodeXY[0]), Integer.parseInt(NodeXY[1])).setAdjNodes(mapNodesAdj);
                }
            }
        }

    }


    public static int getIndex(int x, int y) {
        int index = (x) + (y * indoorMapImage.getWidth());
        return (index >= 0 && index < getImageSize()) ? index : -1;
    }

    public static int getImageSize() {
        return (indoorMapImage.getWidth() * indoorMapImage.getHeight());
    }

    public static void InsertNewMapNode(int x, int y) {
        nodeKeyMapNodeHashtable.put(new NodeKey(x, y), new MapNode(x, y));
    }

    public static MapNode getMapNode(int x, int y) {
        return nodeKeyMapNodeHashtable.get(new NodeKey(x, y));
    }

    public static Bitmap getIndoorMapImage() {
        return indoorMapImage;
    }

    public static void setIndoorMapImage(Bitmap mIndoorMapImage) {
        indoorMapImage = mIndoorMapImage;
    }

    public static Hashtable<NodeKey, MapNode> getNodeKeyMapNodeHashtable() {
        return nodeKeyMapNodeHashtable;
    }

    public static void setNodeKeyMapNodeHashtable(Hashtable<NodeKey, MapNode> nodeKeyMapNodeHashtable) {
        GraphMap.nodeKeyMapNodeHashtable = nodeKeyMapNodeHashtable;
    }

    public static ArrayList<String> GetAllTags() {
        ArrayList<String> ret = new ArrayList<String>();
        Enumeration<MapNode> mapNodeEnumeration = getNodeKeyMapNodeHashtable().elements();
        while (mapNodeEnumeration.hasMoreElements()) {
            MapNode mapNode = mapNodeEnumeration.nextElement();
            if (!ret.contains(mapNode.NodeTag) && !mapNode.getNodeTag().equals("NA")) {
                ret.add(mapNode.NodeTag);
            }
        }
        Collections.sort(ret);
        return ret;
    }
}
