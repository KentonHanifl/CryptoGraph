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

    int delay = 5;

    private boolean notNotifiedLastHour(Currency c, SharedPreferences database){

        SharedPreferences.Editor editor = database.edit();
        if(database.contains(c.getName())) {
            Log.d(tag,"if");
            editor.putInt(c.getName(),(database.getInt(c.getName(), 0)+delay));
            editor.commit();
            if(database.getInt(c.getName(),0)>3600)
            {
                editor.remove(c.getName());
                editor.commit();
                return true;
            }
        }
        else{
            Log.d(tag, "else");
            editor.putInt(c.getName(),0);
            editor.commit();
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences notificationPreferences = context.getSharedPreferences("CryptoGraphNotificationData", 0);

        notificationCurrencies = new Database(notificationPreferences);
        notificationCurrencies.loadOnlyCurrencies(notificationCurrenciesList);

        database = new Database(context.getSharedPreferences("TradeViewData",0));
        database.loadOnlyCurrencies(Currencies);

        for(Currency c : Currencies)
        {
            if(c.favorite && c.getChange()>20f)
            {
                if (notNotifiedLastHour(c, notificationPreferences)){
                    // build notification
                    // the addAction re-use the same intent to keep the example short
                    NotificationCompat.Builder n;
                    n = new NotificationCompat.Builder(context)
                            .setContentTitle(c.getName() + " is up " + String.format("%.2f", c.getChange()))
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
