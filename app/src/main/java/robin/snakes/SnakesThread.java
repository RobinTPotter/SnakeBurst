package robin.snakes;

import android.graphics.Canvas;

public class SnakesThread extends Thread {

        SnakesView snakesView;
        private boolean running = false;

        public SnakesThread(SnakesView view) {
            snakesView = view;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            while (running) {

                Canvas canvas = snakesView.getHolder().lockCanvas();

                if (canvas != null) {
                    synchronized (snakesView.getHolder()) {
                        snakesView.drawSomething(canvas);
                    }
                    snakesView.getHolder().unlockCanvasAndPost(canvas);
                }


                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }



}
