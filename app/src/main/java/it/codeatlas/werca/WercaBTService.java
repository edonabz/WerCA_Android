package it.codeatlas.werca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class WercaBTService extends BluetoothLeService {

    public static final String WERCA_BT_SERVICE = "it.codeatlas.werca.service.WercaBTService";

    private static final String TAG = WercaBTService.class.getSimpleName();
    public String mDeviceAddress = "CC:EF:01:31:BD:61";

    private BroadcastReceiver receiver;
    private IntentFilter filter;

    private char ELP_data[];

    public static final String ACTION_ELP_SEND = "it.codeatlas.werca.receiver.intent.action.ELPsend";
    public static final String ELP_DATA  = "ELP_data";

    @Override
    public void onCreate() {
        super.onCreate();
        ELP_data = new char[20];
        Log.d(TAG, "WercaBTService onCreate");
        if(initialize()){
            Log.d(TAG,"BT initialized");
            if(connect(mDeviceAddress))
                Log.d(TAG,"Connected to " + mDeviceAddress);
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"Ricevuto Broadcast");
                ELP_data = intent.getCharArrayExtra(ELP_DATA);
                if(ELP_data != null)
                    bleUARTsend(new String(ELP_data).getBytes());
            }
        };
        Log.d(TAG,"Creato broadcast receiver");
        filter = new IntentFilter(ACTION_ELP_SEND);

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        disconnect();
        close();
        super.onDestroy();
    }
}
