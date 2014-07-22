package it.codeatlas.werca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;


public class WercaBTService extends BluetoothLeService {

    private static final String TAG = WercaBTService.class.getSimpleName();
    public String mDeviceAddress = "CC:EF:01:31:BD:61";

    private BroadcastReceiver receiver;
    private IntentFilter filter, incomingCallFilter, outgoingCallFilter;

    public String incomingNumber, incomingName, outgoingNumber;
    public boolean incomingCall;

    private char ELP_data[];

    public static final String ACTION_ELP_SEND = "it.codeatlas.werca.receiver.intent.action.ELPsend";
    public static final String ELP_DATA  = "ELP_data";
    public static final String NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

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
                if(ACTION_ELP_SEND.equals(intent.getAction())){
                    Log.d(TAG,"Ricevuto Broadcast");
                    ELP_data = intent.getCharArrayExtra(ELP_DATA);
                    if(ELP_data != null)
                        bleUARTsend(new String(ELP_data).getBytes());
                } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                    //CHIAMATA IN ARRIVO
                    if(TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))) {
                        incomingCall = true;
                        Log.d(TAG, "INTENT: " + intent);
                        //Log.d(TAG,"getDataString--> " + intent.getDataString());
                        Log.d(TAG, "intent.getStringExtra(TelephonyManager.EXTRA_STATE)--> " + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
                        //CHIAMATA IN ARRIVO
                        incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        Log.d(TAG, "EXTRA_INCOMING_NUMBER = " + incomingNumber);

                        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
                        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, incomingNumber, null, null);
                        if (cursor.moveToFirst()) {
                            incomingName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        }
                        cursor.close();
                        Log.d(TAG, "INCOMING NAME = " + incomingName);

                        ELP_data = new String("B" + incomingNumber).toCharArray();
                        if (ELP_data != null) {
                            bleUARTsend(new String(ELP_data).getBytes());
                            Log.d(TAG, "Sent incoming NUMBER" + new String(ELP_data));
                        }

                        ELP_data = new String("C" + incomingName).toCharArray();
                        if (ELP_data != null) {
                            bleUARTsend(new String(ELP_data).getBytes());
                            Log.d(TAG, "Sent incoming NAME" + new String(ELP_data));
                        }
                    } else
                    //TODO: non funziona se la chiamata viene accettata o rifiutata
                    //WORKAROUND
                    if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))){
                        //IN CHIAMATA
                        if(incomingCall) {
                            //outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                            bleUARTsend(new String("B" + incomingNumber).getBytes());
                        } else {
                            bleUARTsend(new String("B" + outgoingNumber).getBytes());
                        }
                        bleUARTsend(new String("C  DIALING...").getBytes());
                        Log.d(TAG,"In chiamata IN");

                    } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE)))
                        bleUARTsend(new String("A").getBytes());
                } else if(NEW_OUTGOING_CALL.equals(intent.getAction())) {
                    //CHIAMATA IN USCITA
                    incomingCall = false;
                    Log.d(TAG,"CHIAMATA IN USCITA");
                    outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.d(TAG,outgoingNumber);
                    bleUARTsend(new String("B" + outgoingNumber).getBytes());
                    bleUARTsend(new String("C  DIALING...").getBytes());
                    Log.d(TAG,"In chiamata OUT");

                }
            }
        };
        Log.d(TAG,"Creato broadcast receiver");
        filter = new IntentFilter(ACTION_ELP_SEND);
        incomingCallFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        outgoingCallFilter = new IntentFilter(NEW_OUTGOING_CALL);

        registerReceiver(receiver, filter);
        registerReceiver(receiver, incomingCallFilter);
        registerReceiver(receiver, outgoingCallFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        disconnect();
        close();
        super.onDestroy();
    }
}
