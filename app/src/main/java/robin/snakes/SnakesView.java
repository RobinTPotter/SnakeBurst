package robin.snakes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SnakesView extends SurfaceView {


    private SurfaceHolder surfaceHolder;
    private SnakesThread myThread;
    private Bitmap buffer;
    private Simulation simulation;
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;
    Handler mHandler;
    FullscreenActivity activity;

    public SnakesView(Context context) {
        super(context);
        init();
    }

    public SnakesView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public SnakesView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init();
    }

    public Bitmap getBuffer() {
        return buffer;
    }

    protected Simulation getSimulation() {
        return simulation;
    }

    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    public ScaleGestureDetector getScaleGestureDetector() {
        return scaleGestureDetector;
    }

    private void init() {

        if (getContext() instanceof FullscreenActivity) {
            activity = (FullscreenActivity) getContext();
        }

        message("init");

        myThread = new SnakesThread(this);

        surfaceHolder = getHolder();

        simulation = new Simulation(this);

        setOnTouchListener(simulation);
        gestureDetector = new GestureDetector(getContext(), simulation);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), simulation);

        /*
         */

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                myThread.setRunning(true);
                try {
                    myThread.start();
                } catch (Exception ex) {

                    message(ex.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                // TODO Auto-generated method stub
                buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                myThread.setRunning(false);
                while (retry) {
                    try {
                        myThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        message("thread interrupted");
                    }
                }
            }
        });


        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(String message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                message(message);
            }
        };
    }

    protected void drawSomething(Canvas surfaceCanvas) {

        simulation.updateProperties(getWidth(), getHeight());

        if (buffer != null) {
            simulation.drawMethod(getWidth(), getHeight());
            surfaceCanvas.drawBitmap(buffer, 0, 0, null);
        }

    }



    public void message(CharSequence text) {

        Context context = getContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

    }














}
