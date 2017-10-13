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
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import android.util.Log;

public class ChartActivity extends AppCompatActivity implements AsyncResponse{



    ArrayList<CurrencyChartData> chartList = new ArrayList<CurrencyChartData>();
    ArrayList<CandleEntry> chartData = new ArrayList<CandleEntry>();
    ArrayList<String> chartLabels = new ArrayList<String>();
    CandleStickChart chart;
    CandleDataSet cds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_activity);
        SharedPreferences data;
        data = getSharedPreferences(Main.filename, 0);
        String MarketName = data.getString("ChartMarketName", "USDT-BTC");
        String interval = "hour";

        //Get Market History Chart
        try
        {
            //https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-1ST&tickInterval=hour
            URL marketHistoryURL = new URL("https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName="+MarketName+"&tickInterval="+interval);
            chart = (CandleStickChart) findViewById(R.id.chart);
            AsyncTask<URL, Integer, StringBuffer> feed = new GetFeed(this).execute(marketHistoryURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

    //Poorly Wirtten-- Come Back
    void drawChart()
    {
        int i = 0;
        for (CurrencyChartData data : chartList)
        {
            chartData.add(new CandleEntry(i, data.H, data.L, data.O, data. C));
            i++;
        }
        for (int j = 0; j < i; j++)
        {
            if (j%10==0)
            {
                chartLabels.add(chartList.get(j).getDateTime());
            }
            else
            {
                chartLabels.add("");
            }
        }

        cds = new CandleDataSet(chartData, "");


        cds.setColor(Color.rgb(80, 80, 80));
        cds.setShadowColor(Color.DKGRAY);
        cds.setShadowWidth(0.7f);
        cds.setDecreasingColor(Color.RED);
        cds.setDecreasingPaintStyle(Paint.Style.FILL);
        cds.setIncreasingColor(Color.rgb(122, 242, 84));
        cds.setIncreasingPaintStyle(Paint.Style.FILL);
        cds.setNeutralColor(Color.BLUE);
        //cds.setValueTextColor(Color.RED);

        CandleData data = new CandleData(cds);

        data.setDrawValues(false);

        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(3);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return chartLabels.get((int)value);
            }
        });

        chart.setVisibleXRangeMinimum(24);
        chart.setVisibleXRangeMaximum(100);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.moveViewToX(chartLabels.size());


        chart.invalidate();
    }

    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
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
            if(jsonStringBuffer.length()!=0)
            {
                jsonStringBuffer.delete(0,1); //Delete the comma between each object
            }
        }

        drawChart();
    }
}
