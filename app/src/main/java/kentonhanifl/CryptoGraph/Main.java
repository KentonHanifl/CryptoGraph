package kentonhanifl.CryptoGraph;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;

//ERROR CODES
//ERR1: Error loading currency. Clear app data and start again.

public class Main extends AppCompatActivity implements AsyncResponse{

    static boolean sortedByName = true;
    static boolean sortedByPrice = false;
    static boolean sortedByChange = false;

    static String tag = "DEBUGAROO"; //For debug messages

    int lock = 0; //Lock on the refresh button

    public static ArrayList<Currency> Currencies = new ArrayList<Currency>();
    public ArrayList<Currency> USDTCurrencies = new ArrayList<Currency>();
    public static ArrayList<Currency> AdapterCurrencies = new ArrayList<Currency>();

    //Filename for shared preferences
    final static public String filename = "TradeViewData";
    static SharedPreferences data;
    int dataSize;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Cheap way to get/put an array with shared preferences. Found the implementation at https://stackoverflow.com/questions/7057845/save-arraylist-to-sharedpreferences (The second answer as of 9/23/17)
        Each iteration grabs the data members for each coin and adds it to Currencies
        ORDER:
        MarketName
        Last
        favorite
        PrevDay
        */

        data = getSharedPreferences(filename, 0);
        dataSize = data.getInt("SIZE", 0);

        Currency temp;
        for(int i = 0; i < dataSize; i++)
        {
            temp = new Currency();
            temp.MarketName = data.getString("MarketName_"+i, "ERR1");
            temp.Last = data.getFloat("Last_"+i, 0);
            temp.favorite = data.getBoolean("Favorite_"+i, false);
            temp.PrevDay = data.getFloat("PrevDay_"+i, 0);
            if (Currencies.indexOf(temp)== -1)
            {
                Currencies.add(temp);
                if(temp.MarketName.startsWith("USDT-"))
                {
                    USDTCurrencies.add(temp);
                }
            }
        }
        Collections.sort(Currencies, new CurrencyNameCompare());


        //See if we're connected to the internet
        //Taken from https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html#DetermineType
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                                    activeNetwork.isConnectedOrConnecting();

        //If we are connected, try to get the feed for the markets.
        if (isConnected)
        {
            try
            {
                AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed(this)
                                                                    .execute(new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast toast = Toast.makeText(this, "No internet connection. Loading data if available.", Toast.LENGTH_SHORT);
            toast.show();
            if (dataSize != 0)
            {
                //Updating the ListView
                setListAdapter();

                //Start the scrolling banner for USDT prices
                startUSDTBanner();
            }

        }

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);

                if(lock==0 && tableSearchBar.isIconified()) //There were bugs letting the user refresh while the SearchView was pressed, so I just disable it here.
                {
                    ConnectivityManager cm = (ConnectivityManager)
                                                Main.this.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //Check if we have network activity before trying to fetch data
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    final boolean isConnected = activeNetwork != null &&
                                                activeNetwork.isConnectedOrConnecting();

                    lock++;
                    if (isConnected)
                    {
                        try
                        {
                            AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed(Main.this)
                                                                                .execute(new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Toast toast = Toast.makeText(Main.this, "No connection. Cannot refresh.", Toast.LENGTH_SHORT);
                        toast.show();
                        lock = 0;
                    }
                }
            }
        });
    }


    /*
    --------------------------------------------------------------------------------
    These are general functions outside of the onCreate() method.
    --------------------------------------------------------------------------------
    */

    //Sets the adapter for the list so that data is actually displayed.
    //Sets the SearchView onclick listener
    //Sets the sorting buttons
    public void setListAdapter()
    {
        sortedByName = true;
        sortedByPrice = false;
        sortedByChange = false;
        AdapterCurrencies.clear();
        AdapterCurrencies.addAll(Currencies);
        ListView list = (ListView) findViewById(R.id.list);
        final CustomAdapter adapter = new CustomAdapter(AdapterCurrencies, Main.this, data);
        list.setAdapter(adapter);
        list.deferNotifyDataSetChanged();

        //Set the onclick for the searchview
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


        /*
        ---------------------------
        SORTING BUTTONS
        ---------------------------
        */

        //-------SORTING BY NAME
        Button sortByName = (Button) findViewById(R.id.sortName);
        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sortedByName) {
                    Collections.sort(AdapterCurrencies, new BackwardsCurrencyNameCompare());
                }
                else
                {
                    Collections.sort(AdapterCurrencies, new CurrencyNameCompare());
                }

                adapter.notifyDataSetChanged();

                sortedByName = !sortedByName;
                sortedByPrice = false;
                sortedByChange = false;
            }
        });

        //-------SORTING BY PRICE
        Button sortByPrice = (Button) findViewById(R.id.sortPrice);
        sortByPrice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (sortedByPrice) {
                    Collections.sort(AdapterCurrencies, new BackwardsCurrencyPriceCompare());
                }
                else
                {
                    Collections.sort(AdapterCurrencies, new CurrencyPriceCompare());
                }

                adapter.notifyDataSetChanged();

                sortedByPrice = !sortedByPrice;
                sortedByName = false;
                sortedByChange = false;
            }
        });

        //-------SORTING BY FAVORITE
        Button sortByFavorite = (Button) findViewById(R.id.sortFavorites);
        sortByFavorite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Collections.sort(AdapterCurrencies, new CurrencyFavoriteCompare());
                adapter.notifyDataSetChanged();

                sortedByName = false;
                sortedByPrice = false;
                sortedByChange = false;
            }
        });

        //-------SORTING BY CHANGES
        Button sortByChange = (Button) findViewById(R.id.sortChanges);
        sortByChange.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (sortedByChange) {
                    Collections.sort(AdapterCurrencies, new BackwardsCurrencyChangeCompare());
                }
                else
                {
                    Collections.sort(AdapterCurrencies, new CurrencyChangeCompare());
                }

                adapter.notifyDataSetChanged();

                sortedByChange = !sortedByChange;
                sortedByName = false;
                sortedByPrice = false;
            }
        });
    }



    //Starts the horizontal scrolling for the USDT banner
    public void startUSDTBanner()
    {
        Collections.sort(USDTCurrencies, new CurrencyNameCompare());
        TextView banner = (TextView) findViewById(R.id.USDTBanner);
        StringBuffer bannerStream = new StringBuffer();
        for(Currency c : USDTCurrencies)
        {
            bannerStream.append("| " + c.getName() + ":  " + String.format("%.2f", c.Last)+ " |      ");
        }
        banner.setText(bannerStream.toString());

        banner.setHorizontallyScrolling(true);
    }

    //Should be called whenever data is changed in any way. Currently just saves EVERYTHING to shared preferences.
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
            editor.remove("PrevDay_"+i);
            editor.putFloat("PrevDay_"+i, Currencies.get(i).PrevDay);
            i++;
        }
        editor.apply();
    }



    /*
    Handling the AsyncTask data
    Builds up the list of currencies from the returned JSON objects
    */
    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
        //Using Gson to parse the JSON object after cleaning it up.
        //I.E. the HTTP response comes with a bunch of useless stuff before the objects we want.
        jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[")+1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
        jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}")+1);

        Gson gsonout = new Gson();
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
                //Take out the ETH markets
                if(!c.MarketName.startsWith("ETH-"))
                {
                    Currencies.add(c); //Add the newly parsed JSON object turned into a currency into the list
                }

                if(c.MarketName.startsWith("USDT-")) //Add to separate list for the banner too if it's a USDT market
                {
                    USDTCurrencies.add(c);
                }
            }
            else
            {
                //Take out the ETH markets
                if(!c.MarketName.startsWith("ETH-"))
                {
                    Currencies.get(Currencies.indexOf(c)).Last = c.Last;
                    Currencies.get(Currencies.indexOf(c)).PrevDay = c.PrevDay;
                }
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


