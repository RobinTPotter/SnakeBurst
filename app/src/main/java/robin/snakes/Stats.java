package robin.snakes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by deadmeat on 15/03/17.
 */
public class Stats extends Dialog {
    Simulation simulation;

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
update(getWorm());
    }

    public void update(Worm worm) {

        if (worm==null) return;
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        TextView txtDirection = findViewById(R.id.txtDirection);
        TextView txtEaten = findViewById(R.id.txtEaten);
        TextView txtReproduced = findViewById(R.id.txtReproduced);

        txtSpeed.setText(String.valueOf(worm.speed));
        txtDirection.setText(String.valueOf(worm.dirx) + "," + String.valueOf(worm.diry));
        txtEaten.setText(String.valueOf(worm.eaten));
        txtReproduced.setText(String.valueOf(worm.reproduced));

    }

    protected void onStart() {

    }

}
