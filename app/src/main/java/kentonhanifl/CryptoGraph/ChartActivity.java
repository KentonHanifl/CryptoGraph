package kentonhanifl.CryptoGraph;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static kentonhanifl.CryptoGraph.Main.tag;

public class ChartActivity extends AppCompatActivity implements AsyncResponse, CompoundButton.OnCheckedChangeListener,OnChartValueSelectedListener{

    private ArrayList<CurrencyChartData> chartList = new ArrayList<CurrencyChartData>();
    private ArrayList<String> chartLabels = new ArrayList<String>();
    private CombinedChart chart;
    private static String MarketName;
    private static String Interval;

    private boolean volumeEnabled;

    private int maxValue;

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
        //Volume disabled to start, but can be enabled.
        volumeEnabled = false;
        ToggleButton volumeToggle = (ToggleButton) findViewById(R.id.volumeToggle);
        volumeToggle.setOnCheckedChangeListener(this);

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


        chart = (CombinedChart) findViewById(R.id.chart);
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
    /*
    --------------------------
    Toggle button callback
    --------------------------
    */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        //So I just use the toggle button like a normal button to have the graphic,
        //but we HAVE to have the isChecked boolean for the callback function...
        Log.d(tag, "k");
        toggleVolumeEnabled();
    }

    /*
    --------------------------
    Radio button Callbacks
    --------------------------
    */
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

    /*
    --------------------------
    Get the data. Mostly just executes GetFeed.
    --------------------------
    */

    void getChartData(String MarketName, String interval)
    {
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
        //Legend, description
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);

        //If volume is enabled, enable the left axis. Otherwise, don't.
        if(volumeEnabled)
        {
            chart.getAxisLeft().setEnabled(true);
        }
        else
        {
            chart.getAxisLeft().setEnabled(false);
        }

        //chart.setPinchZoom(false);

        //Auto scales the chart when scrolled to left or right
        chart.setAutoScaleMinMaxEnabled(true);

        //Binding the data to the chart
        CombinedData data = new CombinedData();
        data.setData(getCandleData());
        if(volumeEnabled)
        {
            data.setData(getVolumeData());
        }
        chart.setData(data);

        //Set the xAxis position
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);

        //This is used to control the amount of X-Axis labels on the actual screen at one time.
        //3 is a number chosen arbitrarily, but 4 did not look good at all.
        xAxis.setLabelCount(3, true);

        //This formats the labels on the x-axis
        //It sets them to show the timestamp for each candle
        maxValue=chartLabels.size();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                try{
                    return chartLabels.get((int)value%maxValue);
                }catch(IndexOutOfBoundsException e){
                    //Can be safely ignored, the exception is caused by clearing the chart.
                    return "";
                }
            }
        });

        //This formats the labels on the y-axis
        //It sets them to show 8 decimal places for non-USDT markets and 2 for USDT markets
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

        //chart.getAxisLeft().setSpaceTop(.3f); //I want to decrease the height of the volume, but I don't know how. This doesn't work.
        chart.getAxisLeft().setSpaceBottom(0);

        chart.notifyDataSetChanged();
        chart.invalidate();

        //These numbers are chosen arbitrarily and could be easily changed
        //More than 300 item on the screen leads to a pretty big performance hit though
        float minrange=6;
        float maxrange=200;

        //(Try to) move the viewport to the end and set the visible range of items
        chart.moveViewToX(chartLabels.size()-maxrange-1);
        chart.setVisibleXRangeMinimum(minrange);
        chart.setVisibleXRangeMaximum(maxrange);
    }

    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
        if (!chart.isEmpty())
        {
            //Clear chart data, labels, and the objects returned from the last GetFeed
            chart.clearValues();
            chart.clear();
            chartLabels.clear();
            chartList.clear();
        }

        try {
            //Using Gson to parse the JSON object after cleaning it up.
            //I.E. the HTTP response comes with a bunch of useless stuff before the objects we want.
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[") + 1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}") + 1);

            Gson gsonout = new Gson();
            //Get each JSON object and put it in the data that we will build our chart out of.
            //If any messages ever contain a open or close curly bracket {}, this will break.
            while (jsonStringBuffer.length() != 0) {
                String json = jsonStringBuffer.substring(0, jsonStringBuffer.indexOf("}") + 1); //Get a (the first) JSON object in the StringBuffer
                CurrencyChartData data = gsonout.fromJson(json, CurrencyChartData.class); //Gson parses the object and puts all of the data into a chart data
                jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("}") + 1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
                chartList.add(data);
                if (jsonStringBuffer.length() != 0) {
                    jsonStringBuffer.delete(0, 1); //Delete the comma between each object
                }
            }
        }catch(NullPointerException e) {
            Toast toast = Toast.makeText(this, "Did not get back data from Bittrex. Cannot load chart.", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
        //Draw the chart once we're done building up the data
        drawChart();
    }



    /*
    ---------------------------------------
    Get data functions- Generates the data for the chart.
    ---------------------------------------
    */

    private CandleData getCandleData()
    {
        ArrayList<CandleEntry> candleData = new ArrayList<CandleEntry>();
        CandleDataSet cds;

        int i = 0;
        for (CurrencyChartData data : chartList)
        {
            candleData.add(i, new CandleEntry(i, data.H, data.L, data.O, data.C));
            chartLabels.add(data.getDateTime());
            i++;
        }

        cds = new CandleDataSet(candleData, "");

        //Formatting the chart's colors
        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setShadowWidth(0.7f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Paint.Style.FILL);
        cds.setNeutralColor(Color.BLUE);

        cds.setAxisDependency(chart.getAxisRight().getAxisDependency());

        CandleData data = new CandleData(cds);

        //Don't draw labels on the candlesticks themselves
        data.setDrawValues(false);

        data.setHighlightEnabled(true);

        return data;
    }

    private BarData getVolumeData()
    {
        ArrayList<BarEntry> volumeData = new ArrayList<BarEntry>();
        BarDataSet bds;

        int i = 0;
        for (CurrencyChartData data : chartList)
        {
            volumeData.add(i, new BarEntry(i, data.BV));
            //chartLabels.add(data.getDateTime()); //Not sure if this does anything
            i++;
        }

        bds = new BarDataSet(volumeData, "");

        bds.setColor(Color.rgb(172, 173, 175));
        bds.setAxisDependency(chart.getAxisLeft().getAxisDependency());

        BarData data = new BarData(bds);

        data.setDrawValues(false);

        data.setHighlightEnabled(false);

        return data;
    }

    /*
    ---------------------------------------
    Highlighted Value callbacks on the chart
    ---------------------------------------
    */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int itemIndex = (int)h.getX();
        CurrencyChartData c  = chartList.get(itemIndex);
        setInfoText(c);
    }

    @Override
    public void onNothingSelected() {
        CurrencyChartData c = new CurrencyChartData();
        setInfoText(c);
    }

    /*
    ---------------------------------------
    Setting the text above the chart
    ---------------------------------------
    */
    private void setInfoText(CurrencyChartData c)
    {
        //If it's a USDT market, use 2 decimal places, if not, use 8.
        String formatString;
        if (MarketName.startsWith("USDT-")){
            formatString = "%.2f";
        }
        else{
            formatString = "%.8f";
        }
        open.setText(String.format(formatString,c.O));
        close.setText(String.format(formatString,c.C));
        high.setText(String.format(formatString,c.H));
        low.setText(String.format(formatString,c.L));
        volume.setText(String.format("%.2f",c.BV));
        time.setText(c.getDateTime());
    }

    /*
    ---------------------------------------
    Toggles the volume displayed on the chart and then redraws the chart
    Has to clear the data on the chart, but does not clear the labels and such.
    ---------------------------------------
    */
    private void toggleVolumeEnabled()
    {
        volumeEnabled = !volumeEnabled;
        if (!chart.isEmpty())
        {
            //Clear the chart but keep the labels and objects returned by GetFeed

            //Some of this is probably unnecessary, but the final "chart.setData(null)" is 100% necessary
            //as there was a nullpointer exception when trying to reload the chart after seting the barData.
            chart.getData().clearValues();
            chart.clearValues();
            chart.getData().setData((BarData)null);
            chart.clear();
            chart.setData(null);
            chart.invalidate();
        }

        drawChart();
    }


}
