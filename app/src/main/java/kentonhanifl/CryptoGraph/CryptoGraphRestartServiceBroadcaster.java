package kentonhanifl.CryptoGraph;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static kentonhanifl.CryptoGraph.Main.tag;

public class CryptoGraphRestartServiceBroadcaster extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(tag, "Service Stops");
        context.startService(new Intent(context, CryptoGraphUpdaterService.class));
    }
}
