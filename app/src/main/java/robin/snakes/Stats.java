package robin.snakes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import static java.lang.Math.min;

/**
 * Created by deadmeat on 15/03/17.
 */
public class Stats extends Dialog {
    Simulation simulation;
    Activity activity;

    Worm worm;

    public Worm getWorm() {
        return worm;
    }

    public void setWorm(Worm worm) {
        this.worm = worm;
    }


    public Stats(Context c, Simulation simulation) {
        super(c);
        this.simulation = simulation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.stats_panel);

        getWindow().setLayout(min(simulation.width, 500), min(400, simulation.height));


    }

    public void setActivity(Activity a) {
        this.activity = a;
    }

    public Activity getActivity() {
        return activity;
    }

    public void update() {
        if (!isShowing()) return;
        getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                if (getWorm() != null) {
                                                    Log.d("GRR", "call stats on " + getWorm());
                                                    TextView txtSpeed = findViewById(R.id.txtSpeed);
                                                    TextView txtDirection = findViewById(R.id.txtDirection);
                                                    TextView txtEaten = findViewById(R.id.txtEaten);
                                                    TextView txtReproduced = findViewById(R.id.txtReproduced);

                                                    txtSpeed.setText(String.valueOf(getWorm().speed));
                                                    txtDirection.setText(String.valueOf(getWorm().dirx) + "," + String.valueOf(getWorm().diry));
                                                    txtEaten.setText(String.valueOf(getWorm().eaten));
                                                    txtReproduced.setText(String.valueOf(getWorm().reproduced));

                                                }

                                            } catch (Exception ex) {
                                                Log.e("STATS", ex.getMessage());
                                            }
                                        }

                                    }
        );


    }


    protected void onStart() {

    }

}
