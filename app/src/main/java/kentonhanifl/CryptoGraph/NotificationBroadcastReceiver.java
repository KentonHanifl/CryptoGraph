package kentonhanifl.CryptoGraph;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;
import static kentonhanifl.CryptoGraph.Main.tag;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    ArrayList<Currency> notificationCurrenciesList = new ArrayList<Currency>();
    ArrayList<Currency> Currencies = new ArrayList<Currency>();
    Database notificationCurrencies;
    Database database;
    float threshold;
    int hours;

    int delay = 5;

    private boolean notNotifiedLastHour(Currency c, SharedPreferences database){

        SharedPreferences.Editor editor = database.edit();
        if(database.contains(c.getName())) {
            editor.putInt(c.getName(),(database.getInt(c.getName(), 0)+delay));
            editor.commit();
            if(database.getInt(c.getName(),0)>(hours*3600))
            {
                editor.putInt(c.getName(),0);
                editor.commit();
                return true;
            }
        }
        else{
            editor.putInt(c.getName(),0);
            editor.commit();
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences notificationPreferences = context.getSharedPreferences("CryptoGraphNotificationData", 0);
        //
        notificationCurrencies = new Database(notificationPreferences);
        notificationCurrencies.loadOnlyCurrencies(notificationCurrenciesList);
        SharedPreferences pref = context.getSharedPreferences("CryptoGraphData",0);
        database = new Database(pref);
        database.loadOnlyCurrencies(Currencies);
        String thresholdstring = pref.getString("Threshold","15");
        threshold = Float.valueOf(thresholdstring);
        String stringhours = pref.getString("Hours","1");
        hours = Integer.valueOf(stringhours);

        for(Currency c : Currencies)
        {
            float change = c.getChange();
            String updown = "up ";
            if (change<1f) updown = "down ";
            if(c.favorite && (change>threshold||change<threshold))
            {
                if (notNotifiedLastHour(c, notificationPreferences)){
                    // build notification
                    // the addAction re-use the same intent to keep the example short
                    NotificationCompat.Builder n;
                    n = new NotificationCompat.Builder(context)
                            .setContentTitle(c.getName() + " is " + updown + String.format("%.1f", c.getChange()) + "%")
                            //.setContentText("Subject")
                            .setSmallIcon(R.mipmap.ic_launcher_round);


                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    int id = notificationPreferences.getInt("id", 0);
                    notificationManager.notify(id , n.build());

                    notificationPreferences.edit().putInt("id",notificationPreferences.getInt("id",0)+1).commit();
                }
            }
        }
    }
}
