package kentonhanifl.CryptoGraph;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

    Network(){
        isConnected = false;
    }

    private static boolean isConnected;

    public static boolean isConnected(Context context)
    {
        //Taken from https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html#DetermineType
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


}
