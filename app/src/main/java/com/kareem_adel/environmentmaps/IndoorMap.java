package com.kareem_adel.environmentmaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kareem_adel.environmentmaps.DataTypeHolders.BooleanHolder;
import com.kareem_adel.environmentmaps.DataTypeHolders.DoubleHolder;
import com.kareem_adel.environmentmaps.Nodes.MapNode;
import com.kareem_adel.environmentmaps.Nodes.NodeKey;
import com.kareem_adel.environmentmaps.OptionItem.OptionsItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: document your custom view class.
 */
public class IndoorMap extends View implements View.OnTouchListener {

    ArrayList<MapNode> activeNodes = new ArrayList<MapNode>();
    ArrayList<MapNode> recommendedNodes = new ArrayList<MapNode>();
    ArrayList<String> AllTags;
    LinearGradient linearGradient;
    Bitmap HeatMapImage;
    Canvas HeatMapImageCanvas;
    Bitmap gradientBitmap;
    Compass compass;

    int OriginX = 0;
    int OriginY = 0;
    public Timer mTimerDisplayRoute;


    public IndoorMap(Context context) {
        super(context);
    }

    public IndoorMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IndoorMap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        GraphMap.InitSlowGraph();

        AllTags = GraphMap.GetAllTags();
        setupPreferencesList();

        OriginX = -(GraphMap.getIndoorMapImage().getWidth() / 2);
        OriginY = -(GraphMap.getIndoorMapImage().getHeight() / 2);

        HeatMapImage = Bitmap.createBitmap(GraphMap.getIndoorMapImage().getWidth(), GraphMap.getIndoorMapImage().getHeight(), Config.ARGB_8888);
        HeatMapImageCanvas = new Canvas(HeatMapImage);

        Paint paint = new Paint();
        gradientBitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
        Canvas gradientCanvas = new Canvas(gradientBitmap);
        linearGradient = new LinearGradient(0, 0, 100, 100, new int[]{Color.BLUE, Color.YELLOW, Color.GREEN, Color.rgb(255, 105, 180), Color.RED}, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        gradientCanvas.drawRect(0, 0, 100, 100, paint);

        setOnTouchListener(this);

        compass = new Compass(this.getContext(), this);


        mTimerDisplayRoute = new Timer();
        mTimerDisplayRoute.scheduleAtFixedRate(TimerDisplayRouteTask, 0, 700);

        invalidate();
    }


    ArrayList<String> allPrefs;
    ArrayList<String> userPrefs;

    public void setupPreferencesList() {
        allPrefs = GraphMap.GetAllTags();
        if (allPrefs.size() > 0) {
            Iterator<String> loadedPrefsIterator = allPrefs.iterator();
            while (loadedPrefsIterator.hasNext()) {
                String Pref = loadedPrefsIterator.next();
                if (Pref.equals("NA"))
                    continue;
                OptionsItem mOptionsItem = null;
                for (OptionsItem optionsItem : Navigator.activity.optionsItemsAdapter.optionsItems) {
                    if (optionsItem.OptionName.equals("Preferences")) {
                        mOptionsItem = optionsItem;
                        break;
                    }
                }
                mOptionsItem.optionsItems.add(new OptionsItem(Pref, R.drawable.none, null));
            }
        }
        userPrefs = Navigator.activity.userSettings.userPrefs;
    }

    Handler handler = new Handler();

    TimerTask TimerDisplayRouteTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    GetNewRouteWithNewSource();
                }
            });
        }
    };


    float rAng = 0;

    float scaleFactor = 1f;

    int canvasWidth = 0;
    int canvasHeight = 0;


    long StartTime = 0;
    double PrevLocationX = 0;
    double PrevLocationY = 0;
    double tx = 0;
    double ty = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        //draw Map
        canvas.save();

        canvas.translate(canvasWidth / 2, canvasHeight / 2);
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(OriginX, OriginY);


        if (GraphMap.getIndoorMapImage() != null)
            canvas.drawBitmap(GraphMap.getIndoorMapImage(), 0, 0, null);

        if (HeatMapImage != null && !HeatMapImage.isRecycled()) {
            Paint alphaPaint = new Paint();
            alphaPaint.setAlpha(100);
            canvas.drawBitmap(HeatMapImage, 0, 0, alphaPaint);
        }


        //draw connections
        switch (Navigator.activity.userSettings.NavigationMode) {
            case "fastMode": {
                paint.setColor(Color.GRAY);
                paint.setStrokeWidth(2.0f);
                Enumeration<MapNode> mapNodeEnumeration = GraphMap.getNodeKeyMapNodeHashtable().elements();
                while (mapNodeEnumeration.hasMoreElements()) {
                    MapNode mapNode = mapNodeEnumeration.nextElement();
                    Iterator<MapNode> mapNodeIterator = mapNode.getAdjNodes().iterator();
                    while (mapNodeIterator.hasNext()) {
                        MapNode mapNodeAdj = mapNodeIterator.next();
                        canvas.drawLine(mapNode.getX(), mapNode.getY(), mapNodeAdj.getX(), mapNodeAdj.getY(), paint);
                    }
                }
                break;
            }
            case "slowMode": {

                break;
            }
        }

        //draw shortest path
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5.0f);
        ArrayList<MapNode> tmpActiveMapNodes = activeNodes;
        if (tmpActiveMapNodes.size() >= 2) {
            for (int i = 1; i < tmpActiveMapNodes.size(); i++) {
                MapNode prevNode = tmpActiveMapNodes.get(i - 1);
                MapNode currentNode = tmpActiveMapNodes.get(i);
                canvas.drawCircle(prevNode.getX(), prevNode.getY(), 2.5f, paint);
                canvas.drawLine(prevNode.getX(), prevNode.getY(), currentNode.getX(), currentNode.getY(), paint);
                canvas.drawCircle(currentNode.getX(), currentNode.getY(), 2.5f, paint);
            }
        }


        // draw nodes
        switch (Navigator.activity.userSettings.NavigationMode) {
            case "fastMode": {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(5.0f);
                Enumeration<MapNode> mapNodeEnumeration = GraphMap.getNodeKeyMapNodeHashtable().elements();
                while (mapNodeEnumeration.hasMoreElements()) {
                    MapNode mapNode = mapNodeEnumeration.nextElement();
                    paint.setStrokeWidth(5.0f);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(mapNode.getX(), mapNode.getY(), 5f, paint);
                    if (!mapNode.getNodeName().equals("NA") && !mapNode.getNodeName().equals("link")) {
                        paint.setStrokeWidth(2f);
                        paint.setColor(Color.BLUE);
                        Rect bounds = new Rect();
                        paint.getTextBounds(mapNode.getNodeName(), 0, mapNode.getNodeName().length(), bounds);
                        canvas.drawText(mapNode.getNodeName(), mapNode.getX() - (bounds.width() / 2), mapNode.getY() + bounds.height() + 5, paint);
                        paint.setStrokeWidth(5.0f);
                        paint.setColor(Color.BLACK);
                    }
                }
                break;
            }
            case "slowMode": {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(5.0f);
                Enumeration<MapNode> mapNodeEnumeration = GraphMap.getNodeKeyMapNodeHashtable().elements();
                while (mapNodeEnumeration.hasMoreElements()) {
                    MapNode mapNode = mapNodeEnumeration.nextElement();
                    if (!mapNode.getNodeName().equals("NA") && !mapNode.getNodeName().equals("link")) {
                        paint.setColor(Color.BLACK);
                        paint.setStrokeWidth(5.0f);
                        canvas.drawCircle(mapNode.getX(), mapNode.getY(), 5f, paint);
                        paint.setColor(Color.BLUE);
                        paint.setStrokeWidth(2.0f);
                        Rect bounds = new Rect();
                        paint.getTextBounds(mapNode.getNodeName(), 0, mapNode.getNodeName().length(), bounds);
                        canvas.drawText(mapNode.getNodeName(), mapNode.getX() - (bounds.width() / 2), mapNode.getY() + bounds.height() + 5, paint);
                    }
                }
                break;
            }
        }


        //draw source and destination


        paint.setColor(Color.MAGENTA);
        paint.setStrokeWidth(5.0f);

        if (DestinationNode != null) {
            canvas.drawCircle(DestinationNode.getX(), DestinationNode.getY(), 5f, paint);
        }

        canvas.save();
        //draw Pinpoints of Preferred places ranked by score
        paint.setColor(Color.parseColor("#000000"));
        int x = 1;
        ArrayList<MapNode> tmpRecommendedNodes = recommendedNodes;
        for (MapNode mapNode : tmpRecommendedNodes) {
            String Rank = "" + x++;
            Rect bounds = new Rect();
            paint.getTextBounds(Rank, 0, Rank.length(), bounds);
            canvas.save();
            canvas.translate(mapNode.getX() - 10, mapNode.getY() - 20);
            canvas.scale(0.2778f, 0.2778f);
            canvas.drawBitmap(GraphMap.PinImage, 0, 0, null);
            canvas.restore();
            canvas.drawText(Rank, mapNode.getX() - bounds.width(), mapNode.getY() - 23, paint);
        }
        canvas.restore();


        //arrow
        canvas.save();
        canvas.translate((int) tx, (int) ty);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        canvas.rotate(rAng);
        canvas.translate((int) -45.0 / 2, (int) -45.0 / 2);

        canvas.drawBitmap(GraphMap.ArrowImage, 0, 0, null);
        canvas.restore();

        if (tx != BlueCloudService.currentLocationX || ty != BlueCloudService.currentLocationY) {
            double VecX = (BlueCloudService.currentLocationX - PrevLocationX);
            double VecY = (BlueCloudService.currentLocationY - PrevLocationY);

            long TimeSinceStart = (System.currentTimeMillis() - StartTime);

            if (TimeSinceStart >= 500.0) {
                tx = BlueCloudService.currentLocationX;
                ty = BlueCloudService.currentLocationY;
                PrevLocationX = BlueCloudService.currentLocationX;
                PrevLocationY = BlueCloudService.currentLocationY;
            } else {
                tx = PrevLocationX + VecX * (TimeSinceStart / 500.0);
                ty = PrevLocationY + VecY * (TimeSinceStart / 500.0);
            }
            invalidate();
        }
        canvas.restore();

    }

    MapNode PrevSourceNode;
    MapNode PrevDestinationNode;
    MapNode SourceNode;
    MapNode DestinationNode;

    int oldX;
    int oldY;
    int newX;
    int newY;
    float currentPinchDistance;
    float lastPinchDistance;
    boolean wasMoving = false;
    boolean wasPinching = false;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this == view) {
            switch (motionEvent.getActionMasked()) {

                case MotionEvent.ACTION_DOWN: {
                    oldX = (int) ((int) motionEvent.getX() / scaleFactor);
                    oldY = (int) ((int) motionEvent.getY() / scaleFactor);
                    newX = (int) ((int) motionEvent.getX() / scaleFactor);
                    newY = (int) ((int) motionEvent.getY() / scaleFactor);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (wasPinching || wasMoving) {
                        wasPinching = false;
                        wasMoving = false;

                        //RefreshMap();

                        return true;
                    }
                    int MapNodeX = (int) ((((int) motionEvent.getX() / scaleFactor) - OriginX) - ((canvasWidth / 2) / scaleFactor));
                    int MapNodeY = (int) ((((int) motionEvent.getY() / scaleFactor) - OriginY) - ((canvasHeight / 2) / scaleFactor));

                    MapNode mapNode = null;

                    switch (Navigator.activity.userSettings.NavigationMode) {
                        case "fastMode": {
                            mapNode = GraphMap.getMapNode(MapNodeX, MapNodeY);
                            if (mapNode == null) {
                                Enumeration<MapNode> mapNodeEnumeration = GraphMap.getNodeKeyMapNodeHashtable().elements();
                                if (mapNodeEnumeration.hasMoreElements()) {
                                    MapNode closestMapNode = mapNodeEnumeration.nextElement();
                                    while (mapNodeEnumeration.hasMoreElements()) {
                                        MapNode tmpMapNode = mapNodeEnumeration.nextElement();
                                        double closestDistance = Math.sqrt(Math.pow(closestMapNode.getX() - MapNodeX, 2) + Math.pow(closestMapNode.getY() - MapNodeY, 2));
                                        double tmpDistance = Math.sqrt(Math.pow(tmpMapNode.getX() - MapNodeX, 2) + Math.pow(tmpMapNode.getY() - MapNodeY, 2));
                                        if (closestDistance > tmpDistance) {
                                            closestMapNode = tmpMapNode;
                                        }
                                    }

                                    mapNode = closestMapNode;
                                }

                            }
                            break;
                        }
                        case "slowMode": {
                            if (GraphMap.getIndex(MapNodeX, MapNodeY) > 0 && GraphMap.getIndex(MapNodeX, MapNodeY) < GraphMap.getImageSize())
                                mapNode = GraphMap.mapNodes[GraphMap.getIndex((MapNodeX / GraphMap.NodesMargin) * GraphMap.NodesMargin, (MapNodeY / GraphMap.NodesMargin) * GraphMap.NodesMargin)];
                            break;
                        }
                    }


                    DestinationNode = mapNode;

                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (motionEvent.getPointerCount() == 1 && wasPinching) {
                        newX = (int) ((int) motionEvent.getX() / scaleFactor);
                        newY = (int) ((int) motionEvent.getY() / scaleFactor);
                        wasPinching = false;
                    }
                    if (motionEvent.getPointerCount() == 2) {
                        float initialX = motionEvent.getX(0);
                        float initialY = motionEvent.getY(0);

                        float secondX = motionEvent.getX(1);
                        float secondY = motionEvent.getY(1);

                        currentPinchDistance = (float) Math.sqrt(Math.pow(secondX - initialX, 2) + Math.pow(secondY - initialY, 2));
                        scaleFactor += ((currentPinchDistance - lastPinchDistance) / (1000)) * scaleFactor;
                        lastPinchDistance = currentPinchDistance;
                        wasPinching = true;
                    } else {
                        oldX = newX;
                        oldY = newY;
                        newX = (int) ((int) motionEvent.getX() / scaleFactor);
                        newY = (int) ((int) motionEvent.getY() / scaleFactor);

                        OriginX += newX - oldX;
                        OriginY += newY - oldY;
                    }
                    if (Math.sqrt(Math.pow(newX - oldX, 2) + Math.pow(newY - oldY, 2)) > 10 / scaleFactor) {
                        wasMoving = true;
                    }
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    float initialX = motionEvent.getX(0);
                    float initialY = motionEvent.getY(0);

                    float secondX = motionEvent.getX(1);
                    float secondY = motionEvent.getY(1);

                    lastPinchDistance = (float) Math.sqrt(Math.pow(secondX - initialX, 2) + Math.pow(secondY - initialY, 2));
                    break;
                }

            }


        }

        return true;
    }


    public void GetNewRouteWithNewSource() {
        MapNode mapNode = null;
        int MapNodeX = BlueCloudService.currentLocationX;
        int MapNodeY = BlueCloudService.currentLocationY;

        switch (Navigator.activity.userSettings.NavigationMode) {
            case "fastMode": {

                mapNode = GraphMap.getMapNode(MapNodeX, MapNodeY);
                if (mapNode == null) {
                    Enumeration<MapNode> mapNodeEnumeration = GraphMap.getNodeKeyMapNodeHashtable().elements();
                    if (mapNodeEnumeration.hasMoreElements()) {
                        MapNode closestMapNode = mapNodeEnumeration.nextElement();
                        while (mapNodeEnumeration.hasMoreElements()) {
                            MapNode tmpMapNode = mapNodeEnumeration.nextElement();
                            double closestDistance = Math.sqrt(Math.pow(closestMapNode.getX() - MapNodeX, 2) + Math.pow(closestMapNode.getY() - MapNodeY, 2));
                            double tmpDistance = Math.sqrt(Math.pow(tmpMapNode.getX() - MapNodeX, 2) + Math.pow(tmpMapNode.getY() - MapNodeY, 2));
                            if (closestDistance > tmpDistance) {
                                closestMapNode = tmpMapNode;
                            }
                        }
                        mapNode = closestMapNode;
                    }

                }
                break;
            }
            case "slowMode": {
                if (GraphMap.getIndex(MapNodeX, MapNodeY) > 0 && GraphMap.getIndex(MapNodeX, MapNodeY) < GraphMap.getImageSize()) {
                    mapNode = GraphMap.mapNodes[GraphMap.getIndex((MapNodeX / GraphMap.NodesMargin) * GraphMap.NodesMargin, (MapNodeY / GraphMap.NodesMargin) * GraphMap.NodesMargin)];
                }
                break;
            }
        }

        SourceNode = mapNode;

        if (SourceNode != null && DestinationNode != null) {
            if (PrevSourceNode != SourceNode || PrevDestinationNode != DestinationNode) {
                switch (Navigator.activity.userSettings.NavigationMode) {
                    case "fastMode": {
                        GetShortestPathFast();
                        break;
                    }
                    case "slowMode": {
                        GetShortestPathSlow();
                        break;
                    }
                }
            }
        } else {
            activeNodes = new ArrayList<>();
        }
        invalidate();
    }

    Thread ShortestPathThread;

    public void GetShortestPathSlow() {
        if (ShortestPathThread == null || !ShortestPathThread.isAlive()) {
            ShortestPathThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (SourceNode == null || DestinationNode == null) {
                        activeNodes = (new ArrayList<MapNode>());
                    } else if (SourceNode == DestinationNode) {
                        activeNodes = (new ArrayList<MapNode>());
                    }

                    try {


                        boolean[] isVisited = new boolean[GraphMap.getImageSize()];
                        final double[] reachCost = new double[GraphMap.getImageSize()];
                        Arrays.fill(reachCost, MapNode.INF);
                        reachCost[GraphMap.getIndex(SourceNode.getX(), SourceNode.getY())] = 0;
                        final double[] f_score = new double[GraphMap.getImageSize()];
                        Arrays.fill(f_score, MapNode.INF);
                        f_score[GraphMap.getIndex(SourceNode.getX(), SourceNode.getY())] = 0;
                        boolean isFirstTime = true;

                        Hashtable<MapNode, MapNode> ShortestPathBacktrack = new Hashtable<MapNode, MapNode>();

                        PriorityQueue<MapNode> priorityQueueNodes = new PriorityQueue<>(GraphMap.getIndoorMapImage().getWidth() * GraphMap.getIndoorMapImage().getHeight(), new Comparator<MapNode>() {
                            @Override
                            public int compare(MapNode lhs, MapNode rhs) {
                                if (f_score[GraphMap.getIndex(lhs.getX(), lhs.getY())] > f_score[GraphMap.getIndex(rhs.getX(), rhs.getY())]) {
                                    return 1;
                                } else if (f_score[GraphMap.getIndex(lhs.getX(), lhs.getY())] < f_score[GraphMap.getIndex(rhs.getX(), rhs.getY())]) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                        priorityQueueNodes.add(SourceNode);
                        while (priorityQueueNodes.size() > 0) {
                            MapNode MinCostMapNode = null;
                            if (isFirstTime) {
                                MinCostMapNode = SourceNode;
                                isFirstTime = false;
                            } else {
                                MinCostMapNode = priorityQueueNodes.poll();
                            }

                            isVisited[GraphMap.getIndex(MinCostMapNode.getX(), MinCostMapNode.getY())] = true;

                            if (MinCostMapNode == DestinationNode) {
                                ArrayList<MapNode> ShortestPathRet = new ArrayList<MapNode>();
                                MapNode mapNode = MinCostMapNode;
                                while (ShortestPathBacktrack.get(mapNode) != null) {
                                    ShortestPathRet.add(mapNode);
                                    mapNode = ShortestPathBacktrack.get(mapNode);
                                }
                                ShortestPathRet.add(SourceNode);
                                activeNodes = (ShortestPathRet);
                                PrevSourceNode = SourceNode;
                                PrevDestinationNode = DestinationNode;
                                Navigator.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invalidate();
                                    }
                                });
                                return;
                            }

                            for (MapNode AdjNode : MinCostMapNode.getAdjNodes()) {
                                if (isVisited[GraphMap.getIndex(AdjNode.getX(), AdjNode.getY())])
                                    continue;
                                double newCost = (reachCost[GraphMap.getIndex(MinCostMapNode.getX(), MinCostMapNode.getY())] + Math.sqrt(Math.pow(AdjNode.getX() - MinCostMapNode.getX(), 2) + Math.pow(AdjNode.getY() - MinCostMapNode.getY(), 2)));
                                if (newCost < reachCost[GraphMap.getIndex(AdjNode.getX(), AdjNode.getY())]) {
                                    reachCost[GraphMap.getIndex(AdjNode.getX(), AdjNode.getY())] = newCost;
                                    f_score[GraphMap.getIndex(AdjNode.getX(), AdjNode.getY())] = newCost + Math.sqrt(Math.pow(AdjNode.getX() - SourceNode.getX(), 2) + Math.pow(AdjNode.getY() - SourceNode.getY(), 2));
                                    priorityQueueNodes.remove(AdjNode);
                                    ShortestPathBacktrack.put(AdjNode, MinCostMapNode);
                                }
                                if (!priorityQueueNodes.contains(AdjNode))
                                    priorityQueueNodes.add(AdjNode);
                            }
                        }
                    } catch (NullPointerException e) {
                        return;
                    }
                }
            });
        }
        if (!ShortestPathThread.isAlive())
            ShortestPathThread.start();
    }

    public void GetShortestPathFast() {
        if (ShortestPathThread == null || !ShortestPathThread.isAlive()) {
            ShortestPathThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (SourceNode == null || DestinationNode == null) {
                        activeNodes = (new ArrayList<MapNode>());
                    } else if (SourceNode == DestinationNode) {
                        activeNodes = (new ArrayList<MapNode>());
                    }
                    try {
                        final Hashtable<NodeKey, DoubleHolder> reachCost = new Hashtable<>();
                        Hashtable<NodeKey, BooleanHolder> isVisited = new Hashtable<>();
                        Hashtable<NodeKey, MapNode> tmpNodeKeyMapNodeHashtable = (Hashtable<NodeKey, MapNode>) GraphMap.getNodeKeyMapNodeHashtable().clone();
                        Enumeration<MapNode> mapNodeEnumeration = tmpNodeKeyMapNodeHashtable.elements();
                        while (mapNodeEnumeration.hasMoreElements()) {
                            MapNode mapNode = mapNodeEnumeration.nextElement();
                            reachCost.put(new NodeKey(mapNode.getX(), mapNode.getY()), new DoubleHolder(MapNode.INF));
                            isVisited.put(new NodeKey(mapNode.getX(), mapNode.getY()), new BooleanHolder(false));
                        }
                        reachCost.get(new NodeKey(SourceNode.getX(), SourceNode.getY())).value = 0;
                        boolean isFirstTime = true;
                        PriorityQueue<MapNode> priorityQueueNodes = new PriorityQueue<>(tmpNodeKeyMapNodeHashtable.size(), new Comparator<MapNode>() {
                            @Override
                            public int compare(MapNode lhs, MapNode rhs) {
                                if (reachCost.get(new NodeKey(lhs.getX(), lhs.getY())).value > reachCost.get(new NodeKey(rhs.getX(), rhs.getY())).value) {
                                    return 1;
                                } else if (reachCost.get(new NodeKey(lhs.getX(), lhs.getY())).value < reachCost.get(new NodeKey(rhs.getX(), rhs.getY())).value) {
                                    return -1;
                                }
                                return 0;
                            }
                        });

                        Hashtable<MapNode, MapNode> ShortestPathBacktrack = new Hashtable<MapNode, MapNode>();
                        priorityQueueNodes.add(SourceNode);
                        while (priorityQueueNodes.size() > 0) {
                            MapNode MinCostMapNode = null;
                            if (isFirstTime) {
                                MinCostMapNode = SourceNode;
                                isFirstTime = false;
                            } else {
                                MinCostMapNode = priorityQueueNodes.poll();
                            }
                            isVisited.get(new NodeKey(MinCostMapNode.getX(), MinCostMapNode.getY())).value = true;

                            if (MinCostMapNode == DestinationNode) {
                                ArrayList<MapNode> ShortestPathRet = new ArrayList<MapNode>();
                                MapNode mapNode = MinCostMapNode;
                                while (ShortestPathBacktrack.get(mapNode) != null) {
                                    ShortestPathRet.add(mapNode);
                                    mapNode = ShortestPathBacktrack.get(mapNode);
                                }
                                ShortestPathRet.add(SourceNode);
                                activeNodes = (ShortestPathRet);
                                PrevSourceNode = SourceNode;
                                PrevDestinationNode = DestinationNode;
                                Navigator.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invalidate();
                                    }
                                });
                                return;
                            }

                            for (MapNode AdjNode : MinCostMapNode.getAdjNodes()) {
                                if (isVisited.get(new NodeKey(AdjNode.getX(), AdjNode.getY())).value)
                                    continue;
                                double newCost = (reachCost.get(new NodeKey(MinCostMapNode.getX(), MinCostMapNode.getY())).value + Math.sqrt(Math.pow(AdjNode.getX() - MinCostMapNode.getX(), 2) + Math.pow(AdjNode.getY() - MinCostMapNode.getY(), 2)));
                                if (newCost < reachCost.get(new NodeKey(AdjNode.getX(), AdjNode.getY())).value) {
                                    reachCost.get(new NodeKey(AdjNode.getX(), AdjNode.getY())).value = newCost;
                                    priorityQueueNodes.remove(AdjNode);
                                    ShortestPathBacktrack.put(AdjNode, MinCostMapNode);
                                }
                                if (!priorityQueueNodes.contains(AdjNode))
                                    priorityQueueNodes.add(AdjNode);
                            }
                        }
                    } catch (NullPointerException e) {
                    }
                }
            });
        }
        if (!ShortestPathThread.isAlive())
            ShortestPathThread.start();
    }


}
