package kentonhanifl.CryptoGraph;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static kentonhanifl.CryptoGraph.Main.tag;

public class ChartActivity extends AppCompatActivity implements AsyncResponse, OnChartValueSelectedListener{

    private ArrayList<CurrencyChartData> chartList = new ArrayList<CurrencyChartData>();
    private ArrayList<CandleEntry> chartData = new ArrayList<CandleEntry>();
    private ArrayList<String> chartLabels = new ArrayList<String>();
    private CandleStickChart chart;
    private CandleDataSet cds;
    private static String MarketName;
    private static String Interval;

    private boolean chartisinvalid;

    private float maxValue;

    private TextView open;
    private TextView close;
    private TextView high;
    private TextView low;
    private TextView volume;
    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_activity);

        Database database = Main.database;
        MarketName = database.getMarketName();
        //Current default interval
        Interval = "day";

        //Initial setup of TextViews
        TextView ChartTitle = (TextView) findViewById(R.id.chartTitle);
        ChartTitle.setText(MarketName);
        open = (TextView) findViewById(R.id.open);
        close = (TextView) findViewById(R.id.close);
        high = (TextView) findViewById(R.id.high);
        low = (TextView) findViewById(R.id.low);
        volume = (TextView) findViewById(R.id.volume);
        time = (TextView) findViewById(R.id.time);
        //Set the initial details to empty string or 0.
        CurrencyChartData c = new CurrencyChartData();
        setInfoText(c);


        chart = (CandleStickChart) findViewById(R.id.chart);
        chart.setOnChartValueSelectedListener(this);

        //Get Market History Chart
        boolean isConnected = Network.isConnected(this);
        if (isConnected) {
            getChartData(MarketName, Interval);
        }
        else {
            Toast toast = Toast.makeText(this, "No connection. Cannot load chart.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.dayButton:
                if (checked)
                {
                    Interval = "day";
                    getChartData(MarketName, Interval);
                }
                break;

            case R.id.hourButton:
                if (checked)
                {
                    Interval = "hour";
                    getChartData(MarketName, Interval);
                }
                break;

            case R.id.thirtyMin:
                if (checked)
                {
                    Interval = "thirtyMin";
                    getChartData(MarketName, Interval);
                }
                break;
        }
    }

    void getChartData(String MarketName, String interval)
    {
        //MOVETONETWORK?
        try
        {
            URL marketHistoryURL = new URL("https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName="+MarketName+"&tickInterval="+interval);
            AsyncTask<URL, Integer, StringBuffer> feed = new GetFeed(this).execute(marketHistoryURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    void drawChart()
    {

        float maxY = getMaxYValue();
        float minY = getMinYValue();

        float i = 0;
        for (CurrencyChartData data : chartList)
        {
            chartData.add((int)i, new CandleEntry(i, data.H, data.L, data.O, data.C));
            chartLabels.add(data.getDateTime());
            i++;
        }
        i=0;


        cds = new CandleDataSet(chartData, "");

        //Formatting the chart's colors
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setShadowWidth(0.7f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Paint.Style.FILL);
        cds.setNeutralColor(Color.BLUE);

        cds.setAxisDependency(YAxis.AxisDependency.RIGHT);

        CandleData data = new CandleData(cds);

        //Don't draw labels on the candlesticks themselves
        data.setDrawValues(false);


        //CHANGE THIS
        data.setHighlightEnabled(true);

        //Binding the data to the chart
        chart.setData(data);

        //Refresh the chart
        chart.notifyDataSetChanged();
        chart.invalidate();

        //Set the xAxis position
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);

        //This is used to control the amount of labels on the actual screen at one time. 3 is a number chosen arbitrarily, but 4 did not look good at all.
        xAxis.setLabelCount(3, true);

        //This sets the actual labels on the x-axis
        maxValue=chartLabels.size()-1;

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //--------------------BUG--------------------
                //There is a weird error where it throws values past the last label for some reason.
                //This is filtering for those bad throws so the app doesn't crash.
                //Pretty sure it has to do with the chart.moveToX() call down below, but I can't figure out why it only does it sometimes.
                //This is a temporary solution that will most likely become a permanent one unfortunately.
                if (chartisinvalid||chartLabels.size()==0)
                {
                    return "";
                }
                if (maxValue<value)
                {
                    return chartLabels.get((int)maxValue);
                }
                return chartLabels.get((int)value);
            }
        });


        YAxis yAxis = chart.getAxisRight();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (MarketName.startsWith("USDT-"))
                {
                    return String.format("%.2f",value);
                }
                return String.format("%.8f", value);
            }
        });

        chartisinvalid=false;

        //Legend, description, and the left y axis are all unnecessary.
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        //Auto scales the chart when scrolled to left or right
        chart.setAutoScaleMinMaxEnabled(true);

        //These numbers too are chosen arbitrarily
        float minrange=6;
        float maxrange=200;

        //Pretty sure this is broken and related to the bug mentioned above.
        //The bug is that on the chart loading up, the chart will be focused a little after the last data point.
        //Doesn't really affect the app too bad and is fixed as soon as the user scrolls.
        chart.moveViewToX(chartLabels.size()-maxrange-1);
        chart.setVisibleXRangeMinimum(minrange);
        chart.setVisibleXRangeMaximum(maxrange);
    }

    private float getMaxYValue() {
        CurrencyChartData d = Collections.max(chartList, new Comparator<CurrencyChartData>() {
            @Override
            public int compare(CurrencyChartData lhs, CurrencyChartData rhs) {
                if (lhs.H > rhs.H) return 1;
                if (lhs.H < rhs.H) return -1;
                return 0;
            }
        });
        return d.H;
    }

    private float getMinYValue() {
        CurrencyChartData d = Collections.max(chartList, new Comparator<CurrencyChartData>() {
            @Override
            public int compare(CurrencyChartData lhs, CurrencyChartData rhs) {
                if (lhs.L > rhs.L) return 1;
                if (lhs.L < rhs.L) return -1;
                return 0;
            }
        });
        return d.L;
    }

    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
        if (!chart.isEmpty())
        {
            chartisinvalid=true;
            chart.clearValues();
            chart.clear();
            chartData.clear();
            chartLabels.clear();
            chartList.clear();
        }

        //Using Gson to parse the JSON object after cleaning it up.
        //I.E. the HTTP response comes with a bunch of useless stuff before the objects we want.
        jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[") + 1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
        jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}") + 1);

        Gson gsonout = new Gson();
        //If any messages ever contain a open or close curly bracket {}, this will break.
        while (jsonStringBuffer.length() != 0) {
            String json = jsonStringBuffer.substring(0, jsonStringBuffer.indexOf("}") + 1); //Get a (the first) JSON object in the StringBuffer
            CurrencyChartData data = gsonout.fromJson(json, CurrencyChartData.class); //Gson parses the object and puts all of the data into a chart data
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("}") + 1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
            chartList.add(data);
            Log.d(tag, data.getDateTime() + " " +String.format("%.2f", data.C));
            if(jsonStringBuffer.length()!=0)
            {
                jsonStringBuffer.delete(0,1); //Delete the comma between each object
            }
        }

        //Draw the chart once we're done building up the data
        drawChart();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int itemIndex = (int)h.getX();
        Log.d(tag, String.valueOf(itemIndex));
        CurrencyChartData c  = chartList.get(itemIndex);
        setInfoText(c);
    }

    @Override
    public void onNothingSelected() {
        CurrencyChartData c = new CurrencyChartData();
        setInfoText(c);
    }

    public void setInfoText(CurrencyChartData c)
    {
        open.setText(String.format("%.8f",c.O));
        close.setText(String.format("%.8f",c.C));
        high.setText(String.format("%.8f",c.H));
        low.setText(String.format("%.8f",c.L));
        volume.setText(String.format("%.2f",c.V));
        time.setText(c.getDateTime());
    }
}
