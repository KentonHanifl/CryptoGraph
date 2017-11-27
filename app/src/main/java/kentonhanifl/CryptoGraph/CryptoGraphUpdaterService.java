package kentonhanifl.CryptoGraph;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
        //Log.d(tag, "service start");
    }

    public CryptoGraphUpdaterService() {
    }

    //IBinder and callback code taken from:
    //https://stackoverflow.com/questions/23586031/calling-activity-class-method-from-service-class
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private LiveUpdaterCallbacks serviceCallbacks;


    // Class used for the client Binder.
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
        //Log.d(tag, "ondestroy");
        Intent broadcastIntent = new Intent("ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);

        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 3 seconds
        timer.schedule(timerTask, 5000, 5000);
    }

    /**
     * timer is set to 3 seconds
     */
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

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void processFinish(StringBuffer jsonStringBuffer) {
        try {
            Log.d(tag, "PF");
            database = new Database(getSharedPreferences("TradeViewData", 0));
            database.loadOnlyCurrencies(Currencies);

            //Log.d(tag, "processFinish()");
            //The HTTP response comes with a bunch of useless stuff before the objects we want. Clean it up.
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[") + 1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}") + 1);

            Gson gsonout = new Gson();
            while (jsonStringBuffer.length() != 0) {
                Currency c = getNextJSONObject(gsonout, jsonStringBuffer);
                appendCurrency(c);
            }

            database.save(Currencies);

            pushCurrencies();

            //Notification receiver
            Intent broadcastIntent = new Intent("ActivityRecognition.NotificationSensor");
            sendBroadcast(broadcastIntent);

        } catch (NullPointerException e) {
            //If we did not get data, or did not get good data, there is nothing we can do.
        }

        lock = false;
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

    private void pushCurrencies() {
        //Try pushing to shared preferences on onStop()/onPause and onResume()/onStart().
        //May need to do MUTEX
        //Log.d(tag, "ENTERpushCurrencies()");
        if(serviceCallbacks!=null)
        {
            Log.d(tag, "push");
            //Log.d(tag, "LEAVEpushCurrencies()");
            serviceCallbacks.update();
        }
            lock=false;
        }
    }

