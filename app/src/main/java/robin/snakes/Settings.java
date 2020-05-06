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

/**
 * Created by deadmeat on 15/03/17.
 */
public class Settings extends Dialog {
    Simulation simulation;

    public Settings(Context c, Simulation simulation) {
        super(c);
        this.simulation = simulation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.burst_settings_panel);

        RadioButton radioBurstScatter = (RadioButton) findViewById(R.id.radioBurstScatter);
        radioBurstScatter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulation.burstStyle = Simulation.BURST_TYPE_SCATTER;
            }
        });

        RadioButton radioBurstRadial = (RadioButton) findViewById(R.id.radioBurstRadial);
        radioBurstRadial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulation.burstStyle = Simulation.BURST_TYPE_RADIAL;
            }
        });

        RadioButton radioBurstSeek = (RadioButton) findViewById(R.id.radioBurstSeek);
        radioBurstSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulation.burstStyle = Simulation.BURST_TYPE_SEEK;
            }
        });

        final EditText burstSize = (EditText) findViewById(R.id.editBurstSize);
        burstSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int t=simulation.burstSize;
                try {
                    t = Integer.parseInt(s.toString());
                    simulation.burstSize=t;
                    burstSize.setBackgroundColor(Color.WHITE);
                }catch(NumberFormatException ex){
                    burstSize.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    protected void onStart() {

    }

}
