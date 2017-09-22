package kentonhanifl.tradingviewmobile;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Main extends AppCompatActivity {
    int a = 0; //Lock on the refresh button
    public ArrayList<Currency> Currencies = new ArrayList<Currency>();

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
    }

    public void setListAdapter()
    {
        ListView list = (ListView) findViewById(R.id.list);
        final customAdapter adapter = new customAdapter(Currencies, Main.this);
        list.setAdapter(adapter);
        list.deferNotifyDataSetChanged();

        SearchView myFilter = (SearchView) findViewById(R.id.tableSearchBar);

        myFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s.toString());
                return true;
            }
        });
    }


    /*
    This is the asynchronous task for fetching market data.
    It makes an HTTP request and puts the data from the returned JSON objects into the ArrayList Currencies
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

        protected void onPostExecute(StringBuffer r) {
            //Using Gson to parse the JSON object after cleaning it up.
            //I.E. the HTTP response comes with a bunch of useless stuff before the objects we want.
            r.delete(0, r.indexOf("[")+1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            r.delete(r.lastIndexOf("]"), r.lastIndexOf("}")+1);

            Gson gsonout = new Gson();
            //-----------Eventually just prices instead of clearing...
            Currencies.clear();
            while(r.length()!=0)
            {
                //If any messages ever contain a open or close curly bracket {}, this will break.
                String json = r.substring(0, r.indexOf("}")+1); //Get a (the first) JSON object in the StringBuffer
                Currency c = gsonout.fromJson(json, Currency.class); //Gson parses the object and puts all of the data into a Currency
                r.delete(0,r.indexOf("}")+1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
                if(r.length()!=0)
                {
                    r.delete(0,1); //Delete the comma between each object
                }
                Currencies.add(c); //Add the newly parsed JSON object turned into a currency into the list
            }


            //Updating the ListView

            setListAdapter();

            a=0; //Reset the lock on the refresh button
        }
    }
}


