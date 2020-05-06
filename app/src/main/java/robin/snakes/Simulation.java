package robin.snakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Simulation implements View.OnTouchListener, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener, Worm.WormWrangler {

    SnakesView SnakesView;
    ArrayList<WormTarget> objects = new ArrayList<WormTarget>();

    int width, height;
    int initialWorms = 10;
    Worm selectedWorm;
    Paint blackpaint = new Paint();
    Map<String, PaintableOption> options;
    Map<String, Paint> pallette;
    long lastBurst = 0;

    /**
     * static string for menu options
     */
    static String OPTION_TARGET_NEAREST_TEXT = "Set Target Nearest";
    static String OPTION_UNSET_TARGET_TEXT = "Unset Target";
    static String OPTION_WORMBURST_TEXT = "Burst Settings";
    static String OPTION_SNAKE_STATS = "Snake Stats";

    /**
     * static string for paintable options
     */
    static String STROKED_RECTANGLE = "STROKED_RECTANGLE";
    static String FILLED_CIRCLE = "FILLED_CIRCLE";
    static String SEEK_RADIUS = "SEEK_RADIUS";
    static String SELECT_BOX = "SELECT_BOX";

    /**
     * static string for custom colours
     */
    static String UNDERLAY_GREEN = "UNDERLAY_GREEN";
    static String SELECTED_GREEN = "SELECTED_GREEN";

    /**
     * burst options
     */
    static String BURST_TYPE_SCATTER = "BURST_TYPE_SCATTER";
    static String BURST_TYPE_RADIAL = "BURST_TYPE_RADIAL";
    static String BURST_TYPE_SEEK = "BURST_TYPE_SEEK";

    int burstSize = 10;
    String burstStyle = BURST_TYPE_RADIAL;
    private Stats stats;

    /**
     * designed to place markers under or over the objects in the simulation.
     * they're put in a hashed map
     */
    class PaintableOption {

        String name;
        float value;
        boolean on = true;
        boolean overlay;
        String colour;
        String shape;

        PaintableOption(String name, float value, boolean overlay, String colour, String shape) {
            this.name = name;
            this.value = value;
            this.overlay = overlay;
            this.colour = colour;
            this.shape = shape;
        }

        public void draw(float x, float y, Canvas c) {
            if (shape.equals(STROKED_RECTANGLE))
                c.drawRect(new RectF(x - value, y - value, x + value, y + value), pallette.get(colour));
            else if (shape.equals(FILLED_CIRCLE))
                c.drawCircle(x, y, value, pallette.get(colour));
        }

    }

    /**
     * @param r     red int
     * @param g     green int
     * @param b     blue int
     * @param style paint style object FILL or STROKE etc..
     * @return Paint object for colluring in stuff
     */
    public static Paint newPaint(int r, int g, int b, Paint.Style style) {
        Paint p = new Paint();
        p.setARGB(255, r, g, b);
        p.setStyle(style);
        return p;
    }

    /**
     * constructor for the simulation allows contact between main app activity (for menu etc)
     * sets up the hashmaps for optional paintable decorations and sets up the colour pallette.
     *
     * @param SnakesView
     */
    public Simulation(SnakesView SnakesView) {

        this.SnakesView = SnakesView;

        setupColours();
        setupPaintableOptions();
        addWorm(initialWorms);

        Log.d("Simulation", "Create Simulation");

    }

    /**
     * set up map of paintable options
     */
    private void setupPaintableOptions() {

        options = new HashMap<String, PaintableOption>();
        options.put(SEEK_RADIUS, new PaintableOption(SEEK_RADIUS, 80, false, UNDERLAY_GREEN, FILLED_CIRCLE));
        options.put(SELECT_BOX, new PaintableOption(SELECT_BOX, 30, true, SELECTED_GREEN, STROKED_RECTANGLE));

    }

    /**
     * set up colour palette for use
     */
    private void setupColours() {

        pallette = new HashMap<String, Paint>();
        pallette.put(SELECTED_GREEN, newPaint(0, 255, 0, Paint.Style.STROKE));
        pallette.put(UNDERLAY_GREEN, newPaint(0, 75, 0, Paint.Style.FILL));
    }

    private static float distSquared(float x1, float y1, float x2, float y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);

    }

    protected void updateProperties(int width, int height) {

        Log.d("Simulation", "update props");

        this.width = width;
        this.height = height;
        try {
            if (this.objects != null) {
                synchronized (objects) {
                    for (Iterator<WormTarget> o = objects.iterator(); o.hasNext(); ) {
                        WormTarget w = o.next();
                        if (w != null) {
                            if (w instanceof Worm) {
                                Worm worm = (Worm) w;
                                if (!w.isAlive()) worm.init(width, height);
                                worm.update();
                            }
                        } else {
                            Log.d("Simulation", "worm is null " + w);
                        }
                    }
                }
            } else {
                Log.d("Simulation", "objects null");
            }
        } catch (ConcurrentModificationException e) {
            //SnakesView.message(e.getMessage());
            Message message = SnakesView.mHandler.obtainMessage(0, e.getMessage());
            message.sendToTarget();
        }
    }

    protected void drawMethod(int width, int height) {

        Log.d("Simulation", "draw");
        Canvas c = new Canvas(SnakesView.getBuffer());
        c.drawRect(0, 0, width, height, blackpaint);

        if (selectedWorm != null) {
            for (PaintableOption po : options.values()) {
                if (!po.overlay) {
                    if (po.on && selectedWorm.targetting == null) {
                        po.draw(selectedWorm.getX(), selectedWorm.getY(), c);
                    }
                }
            }
        }

        if (objects != null) {
            synchronized (objects) {
                for (WormTarget w : objects) {
                    if (w != null && w.isAlive()) w.draw(new Canvas(SnakesView.getBuffer()));
                }
            }
        }
        if (selectedWorm != null) {
            for (PaintableOption po : options.values()) {
                if (po.overlay) {
                    if (po.on) {
                        po.draw(selectedWorm.getX(), selectedWorm.getY(), c);
                    }
                }
            }

        }
    }


    /*
     * actions*/

    protected boolean action(String command) {
        if (command.equals(OPTION_TARGET_NEAREST_TEXT)) {
            if (selectedWorm != null) {

                synchronized (objects) {
                    ArrayList<Worm> f = findAllWorms(selectedWorm, options.get(SEEK_RADIUS).value * options.get(SEEK_RADIUS).value);
                    if (f != null) {
                        selectedWorm.nearestWorm = f.get(0);
                        selectedWorm.targetting = selectedWorm.nearestWorm;
                    }
                }

            }
        } else if (command.equals(OPTION_UNSET_TARGET_TEXT)) {
            if (selectedWorm != null) {
                synchronized (objects) {
                    setNewTarget(selectedWorm);
                    selectedWorm.targetting = null;
                }

            }
        } else if (command.equals(OPTION_WORMBURST_TEXT)) {
            synchronized (objects) {
                (new Settings(SnakesView.getContext(), this)).show();

            }
        } else if (command.equals(OPTION_SNAKE_STATS)) {
            synchronized (objects) {
                if (stats==null) stats = new Stats(SnakesView.getContext(), this);
                stats.setWorm(selectedWorm);
                stats.show();

            }
        }
        return false;
    }

   //public void updateStats() {
   //    if (stats!=null && stats.isShowing() ) stats.update(selectedWorm);
   //}

    /*
    gesture methods
     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //SnakesView.message("touch");
        boolean ret = SnakesView.getGestureDetector().onTouchEvent(event);
        ret = SnakesView.getScaleGestureDetector().onTouchEvent(event) || ret;
        return ret;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //SnakesView.message("down");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // SnakesView.message("show press");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // SnakesView.message("single tap up");
        ArrayList<Worm> wms = findAllWorms(e.getX(), e.getY(), options.get(SELECT_BOX).value * options.get(SELECT_BOX).value);
        if (wms != null) {
            selectedWorm = wms.get(0);
            Log.i("Worm", "got worm" + selectedWorm);
        } else selectedWorm = null;

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //SnakesView.message("scroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (this.SnakesView.activity != null) this.SnakesView.activity.toggle();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (selectedWorm!=null) {
            if (Math.abs(selectedWorm.getX()-e1.getX())<100) {
                if (Math.abs(selectedWorm.getY()-e1.getY())<100) {
                    Log.i("ding", "before "+selectedWorm.speed);
                    float speedfactor = 1+(float)Math.sqrt(velocityX*velocityX+velocityY*velocityY)/2000;
                    Log.i("ding", "before "+speedfactor);
                    selectedWorm.targetx+=velocityX/5;
                    selectedWorm.targety+=velocityY/5;

                    if (selectedWorm.targety>selectedWorm.height) selectedWorm.targety=selectedWorm.height;
                    else if (selectedWorm.targety<0) selectedWorm.targety=0;
                    if (selectedWorm.targetx>selectedWorm.width) selectedWorm.targetx=selectedWorm.width;
                    else if (selectedWorm.targetx<0) selectedWorm.targetx=0;

                    selectedWorm.speed *= speedfactor;

                    selectedWorm.update();
                    Log.i("ding", "after "+selectedWorm.speed);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //SnakesView.message("scale");
        long currentTime = detector.getEventTime();
        if (currentTime > (lastBurst + 500)) {
            SnakesView.message("snake burst!");
            snakeBurst((int) (detector.getFocusX()), (int) (detector.getFocusY()));
            lastBurst = currentTime;
        }
        //

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //SnakesView.message("scale begin");

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        //SnakesView.message("scale end");

    }


    private void snakeBurst(int x, int y) {
        //SnakesView.message("long press");
        double rad = 2 * Math.PI / burstSize;
        double ang = Math.random() * rad;
        synchronized (objects) {
            Worm[] newworms = addWorm(burstSize);
            for (Worm w : newworms) {
                w.x = x;
                w.y = y;
                if (burstStyle.equals(BURST_TYPE_RADIAL)) {
                    w.targetx = (float) (w.x + 2 * Math.cos(ang));
                    w.targety = (float) (w.y + 2 * Math.sin(ang));
                    ang += rad;
                } else if (burstStyle.equals(BURST_TYPE_SCATTER)) {
                    w.targetx = (float) (w.x - 0.5 + Math.random());
                    w.targety = (float) (w.y - 0.5 + Math.random());
                } else if (burstStyle.equals(BURST_TYPE_SEEK)) {
                    w.targetting = selectedWorm;
                }
                w.initSegments();
                w.alive = true;
            }
        }


    }

/*
interface methods WormWrangler
 */

    /**
     * add a nuber of worms, these are created un-alive  and must be init()ed
     * at this time the worms are given position
     *
     * @param numWorms
     */
    public Worm[] addWorm(int numWorms) {

        Worm[] addedWorms = new Worm[numWorms];

        for (int ww = 0; ww < numWorms; ww++) {
            Worm w = new Worm(this);
            synchronized (objects) {
                objects.add(w);
            }
            Log.d("Simulation", "worm is " + w);
            addedWorms[ww] = w;
        }
        return addedWorms;

    }

    public Worm addWorm(float x, float y) {

        Worm w = new Worm(this);
        w.x = x;
        w.y = y;
        w.targetx = x;
        w.targety = y;
        //set this worm alive to preserve x,y
        w.alive = true;
        w.initSegments();

        synchronized (objects) {
            objects.add(w);
        }
        Log.d("Simulation", "worm is " + w);
        return w;

    }

    public void setNewTarget(Worm w) {

        Log.d("Simulation", "set target for " + w);
        w.targetx = (float) (Math.random() * width);
        w.targety = (float) (Math.random() * height);

    }

    public Worm findWorm(float x, float y, float dist2) {

        for (WormTarget w : objects) {
            if (w instanceof Worm && distSquared(x, y, w.getX(), w.getY()) < dist2) { //ie 4 px away
                return (Worm) w;
            }
        }
        return null;
    }

    public ArrayList<?> findAll(float x, float y, float dist2, Class type) {

        ArrayList list = new ArrayList();
        float lastnearest = -1;

        for (WormTarget t : objects) {
            if (t.getClass().getName().equals(type.getName())) {

                float d = distSquared(x, y, t.getX(), t.getY());
                if (d != 0.0f && d < dist2) { //ie 4 px away
                    if (lastnearest == -1 || d < lastnearest) {
                        lastnearest = d;
                        list.add(0, t);
                    } else list.add(t);
                }
            }
        }
        if (list.size() > 0) return list;
        else return null;

    }

    public ArrayList<Worm> findAllWorms(float x, float y, float dist2) {
        return (ArrayList<Worm>) findAll(x, y, dist2, Worm.class);
    }

    public void targetMet(Worm worm, WormTarget target) {

        synchronized (objects) {
            setNewTarget(worm);
            if (Math.random() < worm.aggression) {
                objects.remove(target);
                for (WormTarget o : objects) {
                    if (o instanceof Worm) {
                        Worm w = (Worm) o;
                        if (w.targetting == target) {
                            w.targetting = null;
                            setNewTarget(w);
                        }
                    }
                }
                worm.eaten++;
            } else {
                worm.reproduced++;
                if (target instanceof Worm) {
                    ((Worm) target).reproduced++;
                    addWorm(worm.getX(), worm.getY());
                }
            }
        }
    }

    public Worm findWorm(Worm wormy, float dist2) {
        for (WormTarget w : objects) {
            if (w instanceof Worm && distSquared(wormy.x, wormy.y, w.getX(), w.getY()) < dist2) { //ie 4 px away
                return (Worm) w;
            }
        }
        return null;
    }

    public ArrayList<Worm> findAllWorms(Worm wormy, float dist2) {
        ArrayList<Worm> list = new ArrayList<Worm>();
        float lastnearest = -1;
        for (WormTarget _w : objects) {
            if (_w instanceof Worm) {
                Worm w = (Worm) _w;
                float d = distSquared(wormy.x, wormy.y, w.x, w.y);
                if (d != 0.0f && d < dist2) { //ie 4 px away
                    if (lastnearest == -1 || d < lastnearest) {
                        list.add(0, w);
                    } else list.add(w);
                }
            }
        }
        if (list.size() > 0) return list;
        else return null;
    }

}