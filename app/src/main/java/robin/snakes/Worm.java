package robin.snakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

public class Worm implements Comparable<Worm>, WormTarget {

    static float SPEED_LIMIT = 2.0f;
    float x, y;
    boolean alive = false;
    int size;
    float segsize = 8.0f;
    float targetx;
    float targety;
    float dirx;
    float diry;
    float mag;
    float segments[];
    float step = 1f;
    WormTarget targetting;
    Worm nearestWorm;
    float nearestWormDistance;
    float aggression=0.5f;
    int eaten=0;
    int reproduced=0;
    float speed = 0.5f;
    int width;
    int height;

    Paint p = new Paint();
    Simulation wormWrangler;

    public Worm(Simulation ww) {

        Log.d("Worm", "Create worm");
        this.wormWrangler = ww;

        this.size = (int) (30 * Math.random()) + 15;
        this.speed = (float) (Math.random() * 1.5 + 0.5);
        this.aggression = (float) (Math.random() );

        int rr = 0, gg = 0, bb = 0;
        float ll = 0;

        while (ll < 0.75) {

            rr = (int) (Math.random() * 255);
            gg = (int) (Math.random() * 255);
            bb = (int) (Math.random() * 255);

            ll = (0.2126f * (float)rr + 0.7152f * (float)gg + 0.0722f * (float)bb);
        }

        p.setARGB(255, rr, gg, bb);

        segments = new float[size * 2];

    }


    public float     getX() { return x; }
    public float     getY() { return y; }
    public boolean isAlive() { return  alive; }

    public void init(int width, int height) {
        if (alive) return;
        x = (float) (Math.random() * width);
        y = (float) (Math.random() * height);
        targetx = x;
        targety = y;
        alive = true;
        this.width=width;
        this.height=height;
        initSegments();
    }

    public void initSegments() {

        segments = new float[size * 2];
        int sss = 0;
        for (int ss = segments.length - 1; ss > 1; ss -= 2) {
            segments[ss] = y - 0.5f + (float) (Math.random());
            segments[ss - 1] = x - 0.5f + (float) (Math.random());
        }
        update();
    }

    public double distanceToWorm2(Worm w) {
        return (x - w.x) * (x - w.x) + (y - w.y) * (y - w.y);
    }


    public void update() {

        if (!alive) return;

        if (speed>SPEED_LIMIT) speed *= .95;

        if (targetting != null && targetting.isAlive()) {
            targetx = targetting.getX();
            targety = targetting.getY();
        }

        Log.d("Worm", "update worm");

        dirx = targetx - x;
        diry = targety - y;
        mag = (float) (Math.sqrt(dirx * dirx + diry * diry));

        if (mag < 2) {
            wormWrangler.targetMet(this,targetting);
            dirx = targetx - x;
            diry = targety - y;
            mag = (float) (Math.sqrt(dirx * dirx + diry * diry));
        }

        dirx *= 1 / mag;
        diry *= 1 / mag;

        x += dirx * (speed + Math.random() * 0.1);
        y += diry * (speed + Math.random() * 0.1);

        Log.d("Worm", this + " " + x + " " + y);

        if (segments.length == 0) return;

        /*

        last to first
        distance * step of last to next
         */

        segments[0] = x;
        segments[1] = y;

        float[] newsegments = new float[segments.length];
        newsegments[0] = x;
        newsegments[1] = y;

        for (int ss = 2; ss < segments.length; ss += 4) {
            float lastx = segments[ss - 2];
            float lasty = segments[ss - 1];
            float thisx = segments[ss];
            float thisy = segments[ss + 1];
            float dirx2 = thisx - lastx;
            float diry2 = thisy - lasty;
            mag = (float) (Math.sqrt(dirx2 * dirx2 + diry2 * diry2));
            dirx2 *= 1 / mag;
            diry2 *= 1 / mag;
            newsegments[ss] = lastx + dirx2 * (segsize);// + speed);
            newsegments[ss + 1] = lasty + diry2 * (segsize);// + speed);
            if (ss + 3 < segments.length) {
                newsegments[ss + 2] = newsegments[ss];
                newsegments[ss + 3] = newsegments[ss + 1];
            }

        }
/*
        for (int ss = segments.length - 1; ss > 1; ss -= 2) {

            float lasty = segments[ss];
            float lastx = segments[ss - 1];
            float nexty = segments[ss - 2];
            float nextx = segments[ss - 3];
            dirx = nextx - lastx;
            diry = nexty - lasty;
            mag = (float) (Math.sqrt(dirx * dirx + diry * diry));
            dirx *= 1 / mag;
            diry *= 1 / mag;
            segments[ss] += diry * segsize * speed;
            segments[ss - 1] += dirx * segsize * speed;
            // Log.i("worm", " " + segments[ss - 1] + " " + segments[ss]);
        }
*/
        segments = newsegments;
    }

    public void draw(Canvas c) {

        if (!alive) return;
        Log.d("Worm", "draw worm");
        c.drawCircle(x, y, 2, p);
        c.drawLines(segments, p);
    }

    public int compareTo(Worm another) {
        return (int) (distanceToWorm2(another) * 100);
    }

    public interface WormWrangler {
        void setNewTarget(Worm w);
        Worm[] addWorm(int numToAdd);
        Worm addWorm(float x, float y);
        Worm findWorm(float x, float y, float dist2);
        ArrayList<Worm> findAllWorms(float x, float y, float dist2);
        void targetMet(Worm worm, WormTarget target);
    }

}
