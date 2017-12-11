package kentonhanifl.CryptoGraph;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static kentonhanifl.CryptoGraph.Main.tag;

//Much help from:
//https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android

public class CryptoGraphUpdaterService extends Service implements AsyncResponse {
    private ArrayList<Currency> Currencies = new ArrayList<Currency>();
    private boolean lock = false;
    Database database;


    public CryptoGraphUpdaterService(Context applicationContext) {
        super();
    }

    public CryptoGraphUpdaterService() {
    }

    //IBinder and callback code taken from:
    //https://stackoverflow.com/questions/23586031/calling-activity-class-method-from-service-class

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    private LiveUpdaterCallbacks serviceCallbacks;


    //Class used for the client Binder.
    public class LocalBinder extends Binder {
        CryptoGraphUpdaterService getService() {
            // Return this instance of MyService so clients can call public methods
            return CryptoGraphUpdaterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Used to point to the context to send the updates to
    public void setCallbacks(LiveUpdaterCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "Service Stop");
        SharedPreferences pref = getSharedPreferences("CryptoGraphData",0);

        if(!(pref.getBoolean("NotificationTOF",true)))
        {
            Intent broadcastIntent = new Intent("ActivityRecognition.RestartSensor");
            sendBroadcast(broadcastIntent);
            stoptimertask();
        }
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 5 seconds
        timer.schedule(timerTask, 5000, 5000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (!lock) {
                    lock = true;

                    try {
                        AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed(CryptoGraphUpdaterService.this)
                                .execute(new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //Process all of the data we get back from Bittrex
    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
        try {
            Log.d(tag, "PF");
            //Load what data we have already (to keep favorites and what not)
            database = new Database(getSharedPreferences("CryptoGraphData", 0));
            database.loadOnlyCurrencies(Currencies);

            //The HTTP response comes with a bunch of useless stuff before the objects we want. Clean it up.
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[") + 1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}") + 1);

            Gson gsonout = new Gson();
            while (jsonStringBuffer.length() != 0) {
                Currency c = getNextJSONObject(gsonout, jsonStringBuffer);
                appendCurrency(c);
            }

            //Now that we have updated all of the currencies, save them to preferences
            database.save(Currencies);

            //Try to push update to the main activity if it exists
            pushCurrencies();

            //Send signal to the notification broadcastreceiver
            Intent broadcastIntent = new Intent("ActivityRecognition.NotificationSensor");
            sendBroadcast(broadcastIntent);

        } catch (NullPointerException e) {
            //If we did not get data, or did not get good data, there is nothing we can do.
            Log.d(tag,"PF--Except");
        }
        //lock = false;
    }

    /*
    --------------------------------------------------------------------------------
    FUNCTIONS CALLED BY PROCESSFINISH()
    --------------------------------------------------------------------------------
    */

    private Currency getNextJSONObject(Gson parser, StringBuffer JSONObjects)
    {

        String json = JSONObjects.substring(0, JSONObjects.indexOf("}") + 1); //Get the next JSON object in the StringBuffer
        Currency c = parser.fromJson(json, Currency.class); //Gson parses the object and puts all of the data into a Currency
        JSONObjects.delete(0, JSONObjects.indexOf("}") + 1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
        if (JSONObjects.length() != 0) {
            JSONObjects.delete(0, 1); //Delete the comma between each object
        }
        return c;
    }

    //Could switch this to interacting with the shared preferences, but can't remember how to check if item is already in preferences.
    private void appendCurrency(Currency c)
    {
        if (Currencies.indexOf(c) == -1) //If this isn't in our list of currencies (I.E. a new currency was added)
        {
            if (!c.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                Currencies.add(c); //Add the newly parsed JSON object turned into a currency into the list
            }
        }
        else {
            if (!c.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                Currencies.get(Currencies.indexOf(c)).Last = c.Last;
                Currencies.get(Currencies.indexOf(c)).PrevDay = c.PrevDay;
            }
        }
    }

    //Try pushing to the main activity
    private void pushCurrencies() {
        if(serviceCallbacks!=null)
        {
            Log.d(tag, "push");
            serviceCallbacks.update();
        }
            //allow another network request
            lock=false;
        }

    }

