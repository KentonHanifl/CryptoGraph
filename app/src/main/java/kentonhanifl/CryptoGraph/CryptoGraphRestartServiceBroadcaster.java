package kentonhanifl.CryptoGraph;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static kentonhanifl.CryptoGraph.Main.tag;

//Restarts the UpdaterService whenever the service calls its onDestroy() method, broadcasting to this.
public class CryptoGraphRestartServiceBroadcaster extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CryptoGraphUpdaterService.class));
    }
}
