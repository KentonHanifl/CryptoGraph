package kentonhanifl.CryptoGraph;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.NotificationCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

//ERROR CODES
//ERR1: Error loading currency. Clear app data and start again.

public class Main extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, LiveUpdaterCallbacks {
    //Keeping track of the order the list is sorted
    private boolean sortedByName = true;
    private boolean sortedByPrice = false;
    private boolean sortedByChange = false;

    public static String tag = "kk"; //For debug messages to logcat

    public static ArrayList<Currency> Currencies = new ArrayList<Currency>();
    private ArrayList<Currency> BannerCurrencies = new ArrayList<Currency>();
    public static ArrayList<Currency> AdapterCurrencies = new ArrayList<Currency>();
    private CustomAdapter adapter;

    public static Database database;

    private boolean bannerRunning = false;
    private boolean listAdapterSet = false;
    private CryptoGraphUpdaterService updaterService;
    private Intent updaterIntent;
    private boolean bound = false;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;


        updaterService = new CryptoGraphUpdaterService(ctx);
        updaterIntent = new Intent(ctx, updaterService.getClass());

        //This starts the updater service if it isn't running.
        //It starts a foreground service with a notification that cannot be removed.
        if (!isMyServiceRunning(CryptoGraphUpdaterService.class)){
            Log.d(tag, "start");
            startService(updaterIntent);
        }
        else{//Try to restart the service.
            stopService(updaterIntent);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //Load the database
        database = new Database(getSharedPreferences("CryptoGraphData", 0));
        //Load the database (currently loading the USDT markets into the banner)
        database.loadDatabase(Currencies, BannerCurrencies, new BannerCondition<Currency>() {
            @Override
            boolean test(Currency currency) {
                return currency.MarketName.startsWith("USDT-");
            }
        });

        //Have the list be sorted by name initially
        Collections.sort(Currencies, new CurrencyNameCompare());

        if(Currencies.size()!=0)
        {
            startBanner();
            setListAdapter();
        }

        //Set up all of the buttons and searchview

        Button sortName = (Button) findViewById(R.id.sortName);
        sortName.setOnClickListener(this);

        Button sortPrice = (Button) findViewById(R.id.sortPrice);
        sortPrice.setOnClickListener(this);

        Button sortChanges = (Button) findViewById(R.id.sortChanges);
        sortChanges.setOnClickListener(this);

        Button sortFavorites = (Button) findViewById(R.id.sortFavorites);
        sortFavorites.setOnClickListener(this);

        SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);
        tableSearchBar.setOnQueryTextListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);
    }

    /*
    --------------------------------------------------------------------------------
    BUTTONS
    The OnClick method and then every action after.
    --------------------------------------------------------------------------------
    */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sortName:
                if (!isSortButtonLock()) {
                    sortNameButtonAction();
                }
                break;

            case R.id.sortPrice:
                if (!isSortButtonLock()) {
                    sortPriceButtonAction();
                }
                break;

            case R.id.sortChanges:
                if (!isSortButtonLock()) {
                    sortChangesButtonAction();
                }
                break;

            case R.id.sortFavorites:
                if (!isSortButtonLock()) {
                    sortFavoritesButtonAction();
                }
                break;

        }
    }

    private void sortNameButtonAction() {
        if (sortedByName) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyNameCompare());
            Collections.sort(Currencies, new BackwardsCurrencyNameCompare());
        } else {
            Collections.sort(AdapterCurrencies, new CurrencyNameCompare());
            Collections.sort(Currencies, new CurrencyNameCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByName = !sortedByName;
        sortedByPrice = false;
        sortedByChange = false;
    }

    private void sortPriceButtonAction() {
        if (sortedByPrice) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyPriceCompare());
            Collections.sort(Currencies, new BackwardsCurrencyPriceCompare());
        } else {
            Collections.sort(AdapterCurrencies, new CurrencyPriceCompare());
            Collections.sort(Currencies, new CurrencyPriceCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByPrice = !sortedByPrice;
        sortedByName = false;
        sortedByChange = false;
    }

    private void sortChangesButtonAction() {
        if (sortedByChange) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyChangeCompare());
            Collections.sort(Currencies, new BackwardsCurrencyChangeCompare());
        } else {
            Collections.sort(AdapterCurrencies, new CurrencyChangeCompare());
            Collections.sort(Currencies, new CurrencyChangeCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByChange = !sortedByChange;
        sortedByName = false;
        sortedByPrice = false;
    }

    private void sortFavoritesButtonAction() {
        Collections.sort(AdapterCurrencies, new CurrencyFavoriteCompare());
        Collections.sort(Currencies, new CurrencyFavoriteCompare());
        adapter.notifyDataSetChanged();

        sortedByName = false;
        sortedByPrice = false;
        sortedByChange = false;
    }

    private boolean isSortButtonLock() {
        if (AdapterCurrencies.size() != 0) {
            return false;
        }
        return true;
    }

    /*
    --------------------------------------------------------------------------------
    SEARCH BAR
    The onQuery action
    --------------------------------------------------------------------------------
    */

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.getFilter().filter(s);
        return true;
    }

    /*
    --------------------------------------------------------------------------------
    GRAPHICS FUNCTIONS
    The adapter for the ListView and the Banner
    --------------------------------------------------------------------------------
    */

    //Sets the adapter for the list so that data is actually displayed.
    public void setListAdapter() {
        //Start the listAdapter
        sortedByName = true;
        sortedByPrice = false;
        sortedByChange = false;
        AdapterCurrencies.clear();
        AdapterCurrencies.addAll(Currencies);
        ListView list = (ListView) findViewById(R.id.list);
        adapter = new CustomAdapter(AdapterCurrencies, this);
        list.setAdapter(adapter);
        list.deferNotifyDataSetChanged();
        listAdapterSet = true;
    }

    public void updateList() {
        SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);
        if(tableSearchBar.isIconified()) {
            AdapterCurrencies.clear();
            AdapterCurrencies.addAll(Currencies);
            ListView list = (ListView) findViewById(R.id.list);
            if (listAdapterSet) {
                adapter.notifyDataSetChanged();
                list.deferNotifyDataSetChanged();
            }
        }
    }

    //Starts the horizontal scrolling for the banner
    public void startBanner() {
        bannerRunning = true;
        Collections.sort(BannerCurrencies, new CurrencyNameCompare());
        TextView banner = (TextView) findViewById(R.id.Banner);
        StringBuilder bannerStream = new StringBuilder();
        for (Currency c : BannerCurrencies) {
            bannerStream.append("| " + c.getName() + ":  " + String.format("%.2f", c.Last) + " |      ");
        }
        banner.setText(bannerStream.toString());

        banner.setHorizontallyScrolling(true);
    }


    //Should be called whenever data is changed in any way. Currently just saves EVERYTHING to shared preferences.
    //Only used by CustomAdapter, but CustomAdapter should not have access to the main list of currencies.
    //This will be changed when the favoriting logic is moved out of the CustomAdapter
    public static void saveCurrencies() {
        database.save(Currencies);
    }



    @Override
    public void update()
    {
        database.loadDatabase(Currencies, BannerCurrencies, new BannerCondition<Currency>(){
            @Override
            boolean test(Currency currency) {
                return currency.MarketName.startsWith("USDT-");
            }
        });


        if(!bannerRunning) {
            startBanner();
        }
        if(!listAdapterSet)
        {
            setListAdapter();
        }
        else
        {
            updateList();
        }
    }


    /*--------------------------------------------------------------------------------
    Allows the LiveUpdater service see if it needs to update data to the main activity
    --------------------------------------------------------------------------------*/
    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private boolean isUpdaterServiceForeground()
    {
        SharedPreferences pref = getSharedPreferences("CryptoGraphData",0);
        boolean tof = pref.getBoolean("updaterServiceRunning", false);
        return tof;
    }
    //Callbacks for service binding, passed to bindService()
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(tag, "service connected");
            // cast the IBinder and get MyService instance
            CryptoGraphUpdaterService.LocalBinder binder = (CryptoGraphUpdaterService.LocalBinder) service; //Changed from LocalBinder cast to <----
            updaterService = binder.getService();
            bound = true;
            updaterService.setCallbacks(Main.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        // bind to Service

        Intent intent = new Intent(Main.this, CryptoGraphUpdaterService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (bound) {
            updaterService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }


    /*--------------------------------------------------------------------------------
    Menu inflation and callbacks
    --------------------------------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivity(settingsIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this, About.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
