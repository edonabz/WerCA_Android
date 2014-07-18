package it.codeatlas.werca;


import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;


public class WerCANotificationListenerService extends NotificationListenerService {

    private StatusBarNotification mStatusbarNotification;
    private String currentNotificationPkg;
    private static final String TAG = WerCANotificationListenerService.class.getSimpleName();

    private static final String PKG_DIALER = "com.google.android.dialer";
    private static final String PKG_PHONE = "com.android.phone";
    private static final String PKG_MMS = "com.android.mms";
    private static final String PKG_GMAIL = "com.google.android.gm";

    private int num_calls = 0;
    private int num_msg = 0;
    private int num_email = 0;
    private int num_other = 0;

    private char ELP_data[];

    public static final String INTENT_ACTION = "it.codeatlas.werca.receiver.intent.action.ELPsend";
    public static final String INTENT_EXTRA  = "ELP_data";


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        ELP_data = new char[20];
        ELP_data[0] = 'A';
        mStatusbarNotification = sbn;
        Log.d(TAG, "Getting Notification");
        currentNotificationPkg = mStatusbarNotification.getPackageName();

        if (PKG_DIALER.equals(currentNotificationPkg)){
            //VEDREMO
        }
        else if(PKG_MMS.equals(currentNotificationPkg)){
            num_msg++;
            ELP_data[1] = (char) ('0' + num_msg);
        }
        else if (PKG_PHONE.equals(currentNotificationPkg)){
            num_calls++;
        }
        else if (PKG_GMAIL.equals(currentNotificationPkg)){
            num_email++;
        }
        else{
            num_other++;
            ELP_data[4] =  (char) ('0' + num_other);
        }
        Log.d(TAG,"Added. Call=" + num_calls + " SMS=" + num_msg + " Email=" + num_email + " Altro=" + num_other);



        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA,ELP_data);
        sendBroadcast(intent);
        Log.d(TAG,"Ho fatto un broadcast");
        Log.d(TAG,intent.toString());

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        mStatusbarNotification = sbn;
        Log.e(TAG, "Getting Notification");
        currentNotificationPkg = mStatusbarNotification.getPackageName();

        if (PKG_DIALER.equals(currentNotificationPkg)){
            //VEDREMO
        }
        else if(PKG_MMS.equals(currentNotificationPkg)){
            num_msg--;
        }
        else if (PKG_PHONE.equals(currentNotificationPkg)){
            num_calls--;
        }
        else if (PKG_GMAIL.equals(currentNotificationPkg)){
            num_email--;
        }
        else{
            num_other--;
        }
        Log.v(TAG,"REMOVED. Call=" + num_calls + " SMS=" + num_msg + " Email=" + num_email + " Altro=" + num_other);

        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA,ELP_data);
        sendBroadcast(intent);
        Log.d(TAG,"Ho fatto un broadcast");
        Log.d(TAG,intent.toString());
        //sendELP(ELP_data);

    }

}
