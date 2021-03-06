package it.codeatlas.werca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;



public class WercaBTService extends BluetoothLeService {

    private static final String TAG = WercaBTService.class.getSimpleName();
    public String mDeviceAddress = "CC:EF:01:31:BD:61";

    private BroadcastReceiver receiver;

    public String incomingNumber, incomingName, outgoingNumber;
    public boolean incomingCall, inCallState;

    private char ELP_data[];

    public static final String ACTION_ELP_SEND = "it.codeatlas.werca.receiver.intent.action.ELPsend";
    public static final String ELP_DATA  = "ELP_data";
    public static final String NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onCreate() {
        super.onCreate();
        ELP_data = new char[20];
        for(int i = 1; i < 6; i++)
            ELP_data[i] = '0';

        Log.d(TAG, "WercaBTService onCreate");
        if(initialize()){
            Log.d(TAG,"BT initialized");
            sendTimeEveryMinute();
            if(connect(mDeviceAddress))
                Log.d(TAG,"Connected to " + mDeviceAddress);
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //RECEIVER for ELP_data on NOTIFICATIONS
                if(ACTION_ELP_SEND.equals(intent.getAction())){
                    inCallState = false;
                    Log.d(TAG,"ELP broadcast received");
                    ELP_data = intent.getCharArrayExtra(ELP_DATA);
                    if(ELP_data != null) {
                        bleUARTsend(new String(ELP_data).getBytes());
                        Log.d(TAG,"Sent notification ELP_data = " + new String(ELP_data));
                    }

                //RECEIVER for INCOMING CALL
                } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                    inCallState = true;
                    //INCOMING CALL
                    if(TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))) {
                        incomingCall = true;
                        incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        //Log.d(TAG, "EXTRA_INCOMING_NUMBER = " + incomingNumber);
                        //Send incoming call number
                        bleUARTsend(("BI" + incomingNumber).getBytes());
                        Log.d(TAG, "Sent incoming NUMBER" + new String(ELP_data));
                        sendContactName(incomingNumber);

                    } else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))){
                        //DURING THE CALL
                        Log.d(TAG,"CHIAMATA IN CORSO. incomingCall = " + incomingCall);
                        if(incomingCall) {
                            //outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                            bleUARTsend(("BD" + incomingNumber).getBytes());
                            sendContactName(incomingNumber);
                        } else {
                            bleUARTsend(("BO" + outgoingNumber).getBytes());
                            sendContactName(outgoingNumber);
                        }

                    } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))) {
                        //IDLE
                        inCallState = false;
                        //Set actual time and send empty notifications ELP_data
                        ELP_data[0] = 'A';
                        Time now = new Time();
                        now.setToNow();
                        ELP_data[10] = (char) now.hour;
                        ELP_data[11] = ':';
                        ELP_data[12] = (char) now.minute;
                        bleUARTsend(new String(ELP_data).getBytes());
                        //bleUARTsend(("A").getBytes());
                        Log.d(TAG,"EXTRA_STATE_IDLE");
                    }

                    //Query to fetch contact name on incoming call


                } else if(NEW_OUTGOING_CALL.equals(intent.getAction())) {
                    //OUTGOING CALL
                    inCallState = true;
                    incomingCall = false;
                    outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.d(TAG,"CHIAMATA IN USCITA" + outgoingNumber);
                    bleUARTsend(("BO" + outgoingNumber).getBytes());
                    sendContactName(outgoingNumber);

                }

                /*if(inCallState) {
                    //Select number to query if the call is incoming or outgoing
                    if(incomingCall) {
                        inputNumber = incomingNumber;
                        Log.d(TAG,"inputNumber from incomingNumber = " + inputNumber);
                    }
                    else {
                        inputNumber = outgoingNumber;
                        Log.d(TAG,"inputNumber from outgoingNumber = " + inputNumber);
                    }

                }*/

            }
        };

        //Register receivers
        IntentFilter filter = new IntentFilter(ACTION_ELP_SEND);
        IntentFilter incomingCallFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        IntentFilter outgoingCallFilter = new IntentFilter(NEW_OUTGOING_CALL);

        registerReceiver(receiver, filter);
        registerReceiver(receiver, incomingCallFilter);
        registerReceiver(receiver, outgoingCallFilter);
        Log.d(TAG,"Broadcast receivers registered");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        disconnect();
        close();
        super.onDestroy();
    }

    public void sendContactName(String inputNumber){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(inputNumber));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, inputNumber, null, null);
        if (cursor.moveToFirst()) {
            incomingName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();

        bleUARTsend(("C" + incomingName).getBytes());
        Log.d(TAG, "Sent incoming NAME " + "C" + incomingName);
    }


    //VECCHIO METODO INVIO PIANIFICATO ORARIO --> TESTING
    public void sendTimeEveryMinute() {
        final Runnable sendTime = new Runnable() {
            public void run() {
                ELP_data[0] = 'A';
                Time now = new Time();
                now.setToNow();
                ELP_data[10] = (char) now.hour;
                ELP_data[11] = ':';
                ELP_data[12] = (char) now.minute;
                bleUARTsend(new String(ELP_data).getBytes());
                Log.d(TAG,"Sent scheduled time ELP= " + new String(ELP_data));
            }
        };
        final ScheduledFuture<?> timerHandle =
                scheduler.scheduleWithFixedDelay(sendTime, 10, 60, SECONDS);
        scheduler.schedule(new Runnable() {
            public void run() { timerHandle.cancel(true); }
        }, 60 * 60, SECONDS);
    }
}
