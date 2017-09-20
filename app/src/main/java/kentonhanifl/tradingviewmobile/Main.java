package kentonhanifl.tradingviewmobile;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Main extends AppCompatActivity {
    int a = 0;
    ArrayList<Currency> Currencies = new ArrayList<Currency>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            //The worst locking mechanism you've ever seen
            public void onClick(View view) {
                if(a==0)
                {
                    AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();
                }
                a++;
            }
        });
        //

    }





    class GetFeed extends AsyncTask<URL, Integer, StringBuffer> {
        @Override
        protected StringBuffer doInBackground(URL... urls) {
            HttpURLConnection connection = null;
            StringBuffer response = null;
            try {

                URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries");
                connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));
                response = new StringBuffer();
                response.append(in.readLine());
                Log.d("H", response.toString());
                return response;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response;
        }

        protected void onPostExecute(StringBuffer r) {
            r.delete(0, r.indexOf("[")+1);
            r.delete(r.lastIndexOf("]"), r.lastIndexOf("}")+1);

            Gson gsonout = new Gson();
            Currencies.clear();
            while(r.length()!=0)
            {
                String json = r.substring(0, r.indexOf("}")+1);
                Currency c = gsonout.fromJson(json, Currency.class);
                r.delete(0,r.indexOf("}")+1);
                if(r.length()!=0)
                {
                    r.delete(0,1);
                }
                Currencies.add(c);
            }

            for(int i = 0; i<Currencies.size(); i++) {
                ListView list = (ListView) findViewById(R.id.list);
                customAdapter adapter = new customAdapter(Currencies, Main.this);
                list.setAdapter(adapter);



                /*REPLACE--------------------
                TableLayout table = (TableLayout) findViewById(R.id.table);
                TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.tablerow, null);
                if(i%2==0){row.setBackgroundColor(getColor(R.color.rowBackgroundDark));}
                else {row.setBackgroundColor(getColor(R.color.rowBackgroundLight));}
                TextView cell = (TextView) row.findViewById(R.id.tableCell1);
                cell.setText(Currencies.get(i).MarketName);


                cell = (TextView) row.findViewById(R.id.tableCell2);
                cell.setText(String.format("%.8f",Currencies.get(i).Last));

                table.addView(row);
                */
            }

            a=0;
        }
    }


}


