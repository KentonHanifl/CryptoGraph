package kentonhanifl.tradingviewmobile;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//ERR1: Error loading currency. Clear app data and start again.

public class Main extends AppCompatActivity {

    static String tag = "KENTON";

    int lock = 0; //Lock on the refresh button
    public static ArrayList<Currency> Currencies = new ArrayList<Currency>();
    public ArrayList<Currency> USDTCurrencies = new ArrayList<Currency>();


    final public String filename = "TradeViewData";
    static SharedPreferences data;
    int dataSize;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = getSharedPreferences(filename, 0);
        dataSize = data.getInt("SIZE", 0);

        /*
        Cheap way to get/put an array with shared preferences. Found the implementation at https://stackoverflow.com/questions/7057845/save-arraylist-to-sharedpreferences (The second answer as of 9/23/17)
        Each iteration grabs the data members for each coin and adds it to Currencies
        ORDER:
        MarketName
        Last
        favorite
        */

        Currency temp;
        for(int i = 0; i < dataSize; i++)
        {
            temp = new Currency();
            temp.MarketName = data.getString("MarketName_"+i, "ERR1");
            temp.Last = data.getFloat("Last_"+i, 0);
            temp.favorite = data.getBoolean("Favorite_"+i, false);
            if (Currencies.indexOf(temp)== -1)
            {
                Currencies.add(temp);
                if(temp.MarketName.startsWith("USDT-"))
                {
                    USDTCurrencies.add(temp);
                }
            }


        }

        AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);

                if(lock==0 && tableSearchBar.isIconified()) //There were bugs letting the user refresh while the SearchView was pressed, so I just disable it here.
                {
                    lock++;
                    AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed().execute();
                }

            }
        });
    }

    public void setListAdapter()
    {
        ListView list = (ListView) findViewById(R.id.list);
        final CustomAdapter adapter = new CustomAdapter(Currencies, Main.this);
        list.setAdapter(adapter);
        list.deferNotifyDataSetChanged();

        SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);

        tableSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                adapter.getFilter().filter(s);
                return true;
            }
        });
    }

    public void startUSDTBanner()
    {
        TextView banner = (TextView) findViewById(R.id.USDTBanner);
        StringBuffer bannerStream = new StringBuffer();
        for(Currency c : USDTCurrencies)
        {
            bannerStream.append("| " + c.getName() + ":  " + String.format("%.2f", c.Last)+ " |      ");
        }
        banner.setText(bannerStream.toString());
        banner.setHorizontallyScrolling(true);
    }

    public static void saveCurrencies()
    {
        SharedPreferences.Editor editor = data.edit();
        editor.putInt("SIZE", Currencies.size());
        int i = 0;
        for(Currency c : Currencies)
        {
            editor.remove("MarketName_"+i);
            editor.putString("MarketName_"+i, Currencies.get(i).MarketName);
            editor.remove("Last_"+i);
            editor.putFloat("Last_"+i, Currencies.get(i).Last);
            editor.remove("Favorite_"+i);
            editor.putBoolean("Favorite_"+i, Currencies.get(i).favorite);
            i++;
        }
        editor.commit();
    }


    /*
    --------------------------------------------------------------------------------
    This is the asynchronous task for fetching market data.
    It makes an HTTP request and puts the data from the returned JSON objects into the ArrayList Currencies
    --------------------------------------------------------------------------------
    */

    class GetFeed extends AsyncTask<URL, Integer, StringBuffer> {
        @Override
        protected StringBuffer doInBackground(URL... urls) {
            //Get JSON objects
            //We just put the entire HTTP response into a StringBuffer
            HttpURLConnection connection = null;
            StringBuffer response = null;
            try {
                //-------------Eventually will fetch more, but this is fine for now...
                URL url = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries");
                connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));
                response = new StringBuffer();
                response.append(in.readLine());
                return response;
            //------------Exceptions are not fully understood. Will get back to it...
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

        protected void onPostExecute(StringBuffer jsonStringBuffer) {
            //Using Gson to parse the JSON object after cleaning it up.
            //I.E. the HTTP response comes with a bunch of useless stuff before the objects we want.
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[")+1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}")+1);

            Gson gsonout = new Gson();
            //-----------Eventually just prices instead of clearing...
            //Currencies.clear();
            while(jsonStringBuffer.length()!=0)
            {
                //If any messages ever contain a open or close curly bracket {}, this will break.
                String json = jsonStringBuffer.substring(0, jsonStringBuffer.indexOf("}")+1); //Get a (the first) JSON object in the StringBuffer
                Currency c = gsonout.fromJson(json, Currency.class); //Gson parses the object and puts all of the data into a Currency
                jsonStringBuffer.delete(0,jsonStringBuffer.indexOf("}")+1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
                if(jsonStringBuffer.length()!=0)
                {
                    jsonStringBuffer.delete(0,1); //Delete the comma between each object
                }
                if (Currencies.indexOf(c) == -1) //If this isn't in our list of currencies (I.E. a new currency was added)
                {
                    Currencies.add(c); //Add the newly parsed JSON object turned into a currency into the list

                    if(c.MarketName.startsWith("USDT-")) //Add to separate list for the banner too if it's a USDT market
                    {
                        USDTCurrencies.add(c);
                    }
                }
                else
                {
                    if(Currencies.get(Currencies.indexOf(c)).Last!=c.Last)
                    {
                        Log.d(tag, "UPDATINGUPDATINGUPDATINGUPDATINGUPDATING " + c.getName());
                    }
                    Currencies.get(Currencies.indexOf(c)).Last = c.Last;
                    if(c.MarketName.startsWith("USDT-"))
                    {
                        USDTCurrencies.get(USDTCurrencies.indexOf(c)).Last = c.Last;
                    }
                }
            }


            //Updating the ListView
            setListAdapter();

            //Start the scrolling banner for USDT prices
            startUSDTBanner();

            //Save array in shared preferences
            saveCurrencies();

            lock=0; //Reset the lock on the refresh button
        }
    }

}


