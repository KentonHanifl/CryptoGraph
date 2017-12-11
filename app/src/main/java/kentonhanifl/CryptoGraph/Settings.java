package kentonhanifl.CryptoGraph;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;

import java.util.ArrayList;

public class Settings extends AppCompatActivity implements WheelPicker.OnItemSelectedListener, View.OnClickListener {
    Context ctx;
    Switch notificationSwitch;
    TextView currentHours;
    TextView currentThreshold;
    private Context getCtx()
    {
        return ctx;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ctx = this;

        WheelPicker hours = (WheelPicker) findViewById(R.id.HoursPicker);
        WheelPicker threshold = (WheelPicker) findViewById(R.id.ThresholdPicker);
        ArrayList<Integer> hoursData = new ArrayList<Integer>();
        ArrayList<String> thresholdData = new ArrayList<String>();

        for (int i = 1; i < 25; i++) {
            hoursData.add(i);
        }
        hours.setData(hoursData);

        for (int i = 5; i < 30; i += 5) {
            thresholdData.add("\u2213" + Integer.toString(i));
        }
        threshold.setData(thresholdData);

        notificationSwitch = (Switch) findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnClickListener(this);
        currentHours = (TextView) findViewById(R.id.currentHours);
        currentThreshold = (TextView) findViewById(R.id.currentThreshold);
        SharedPreferences pref = getSharedPreferences("CryptoGraphData", 0);
        currentHours.setText(pref.getString("Hours","1"));
        String text = "\u2213 "+pref.getString("Threshold","15");
        currentThreshold.setText(text);
    }


    @Override
    public void onItemSelected(WheelPicker picker, Object data, int position) {
        SharedPreferences pref = getCtx().getSharedPreferences("CryptoGraphData", 0);
        SharedPreferences.Editor edit = pref.edit();
        currentHours = (TextView) findViewById(R.id.currentHours);
        currentThreshold = (TextView) findViewById(R.id.currentThreshold);
        switch (picker.getId()) {
            case R.id.HoursPicker:
                currentHours.setText(data.toString());
                edit.putString("Hours",data.toString());
                break;
            case R.id.ThresholdPicker:
                currentThreshold.setText(data.toString());
                edit.putString("Threshold",data.toString().replace("\u2213",""));
                break;
            default:
                break;
        }
        edit.commit();
    }

    @Override
    public void onClick(View view) {
        SharedPreferences pref = getCtx().getSharedPreferences("CryptoGraphData",0);
        SharedPreferences.Editor edit = pref.edit();
        if (view.getId()==R.id.notificationSwitch)
        {
            if(notificationSwitch.isChecked())
            {

                edit.putBoolean("NotificationTOF",true);
            }
            else{
                edit.putBoolean("NotificationTOF",false);
            }
        }
    }
}
