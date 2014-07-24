package it.codeatlas.werca;


import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.format.Time;
import android.util.Log;

//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//
//import static java.util.concurrent.TimeUnit.MINUTES;
//import static java.util.concurrent.TimeUnit.SECONDS;


public class WerCANotificationListenerService extends NotificationListenerService {

    //private StatusBarNotification mStatusbarNotification;
    private String currentNotificationPkg;
    private static final String TAG = WerCANotificationListenerService.class.getSimpleName();

    private static final String PKG_DIALER = "com.google.android.dialer";
    private static final String PKG_PHONE = "com.android.phone";
    private static final String PKG_MMS = "com.android.mms";
    private static final String PKG_WHATSAPP = "com.whatsapp";
    private static final String PKG_TALK = "com.google.android.talk";
    private static final String PKG_GMAIL = "com.google.android.gm";

    private StatusBarNotification[] notificationList;

    private int num_calls = 0;
    private int num_msg = 0;
    private int num_email = 0;
    private int num_other = 0;

    private char ELP_data[];

    public static final String INTENT_ACTION = "it.codeatlas.werca.receiver.intent.action.ELPsend";
    public static final String INTENT_EXTRA  = "ELP_data";

    //private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"WerCANotificationListenerService onCreate");
        //Avvia pianificazione per invio segnale orario ogni minuto
        //sendTimeEveryMinute();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Time now = new Time();

        notificationList = getActiveNotifications();

        num_msg = 0;
        num_calls = 0;
        num_email = 0;
        num_other = 0;

        if (!PKG_DIALER.equals(sbn.getPackageName())) {
            for (StatusBarNotification notification : notificationList) {       //FOR every notification
                //Log.d(TAG, "onNotificationRemoved notification.toString" + notification.toString());
                currentNotificationPkg = notification.getPackageName();
                Log.d(TAG,"PACKAGE = " + currentNotificationPkg);
                if (PKG_MMS.equals(currentNotificationPkg) || PKG_WHATSAPP.equals(currentNotificationPkg) || PKG_TALK.equals(currentNotificationPkg)) {
                    num_msg++;
                } else if (PKG_PHONE.equals(currentNotificationPkg)) {
                    num_calls++;
                } else if (PKG_GMAIL.equals(currentNotificationPkg)) {
                    num_email++;
                } else {
                    num_other++;
                }

            }

            //Log.d(TAG, "onNotificationPosted Call=" + num_calls + " SMS=" + num_msg + " Email=" + num_email + " Altro=" + num_other);

            ELP_data = new char[20];
            ELP_data[0] = 'A';
            ELP_data[1] = (char) ('0' + num_msg);
            ELP_data[2] = (char) ('0' + num_calls);
            ELP_data[3] = (char) ('0' + num_email);
            ELP_data[4] = (char) ('0' + num_other);
            now.setToNow();
            ELP_data[10] = (char) now.hour;
            ELP_data[11] = ':';
            ELP_data[12] = (char) now.minute;

            Intent intent = new Intent();
            intent.setAction(INTENT_ACTION);
            intent.putExtra(INTENT_EXTRA, ELP_data);
            sendBroadcast(intent);
            //Log.d(TAG, "SENT broadcast " + intent.toString());
            Log.d(TAG, "SENT ELP broadcast = " + new String(ELP_data));

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Time now = new Time();


        notificationList = getActiveNotifications();

        num_msg = 0;
        num_calls = 0;
        num_email = 0;
        num_other = 0;

        if (!PKG_DIALER.equals(sbn.getPackageName())) {
            for (StatusBarNotification notification : notificationList) {       //FOR every notification
                //Log.d(TAG, "onNotificationRemoved notification.toString" + notification.toString());
                currentNotificationPkg = notification.getPackageName();
                Log.d(TAG,"PACKAGE = " + currentNotificationPkg);
                if (PKG_MMS.equals(currentNotificationPkg) || PKG_WHATSAPP.equals(currentNotificationPkg) || PKG_TALK.equals(currentNotificationPkg)) {
                    num_msg++;
                } else if (PKG_PHONE.equals(currentNotificationPkg)) {
                    num_calls++;
                } else if (PKG_GMAIL.equals(currentNotificationPkg)) {
                    num_email++;
                } else {
                    num_other++;
                }

            }

            //Log.d(TAG, "onNotificationRemoved Call=" + num_calls + " SMS=" + num_msg + " Email=" + num_email + " Altro=" + num_other);

            ELP_data = new char[20];
            ELP_data[0] = 'A';
            ELP_data[1] = (char) ('0' + num_msg);
            ELP_data[2] = (char) ('0' + num_calls);
            ELP_data[3] = (char) ('0' + num_email);
            ELP_data[4] = (char) ('0' + num_other);
            now.setToNow();
            ELP_data[10] = (char) now.hour;
            ELP_data[11] = ':';
            ELP_data[12] = (char) now.minute;

            Intent intent = new Intent();
            intent.setAction(INTENT_ACTION);
            intent.putExtra(INTENT_EXTRA, ELP_data);
            sendBroadcast(intent);
            //Log.d(TAG, "SENT broadcast " + intent.toString());
            Log.d(TAG, "SENT ELP broadcast = " + new String(ELP_data));

        }
    }

//    public void sendTimeEveryMinute() {
//        final Runnable sendTime = new Runnable() {
//            public void run() {
//                ELP_data[0] = 'A';
//                Time now = new Time();
//                now.setToNow();
//                ELP_data[10] = (char) now.hour;
//                ELP_data[11] = ':';
//                ELP_data[12] = (char) now.minute;
//                Intent intent = new Intent();
//                intent.setAction(INTENT_ACTION);
//                intent.putExtra(INTENT_EXTRA, ELP_data);
//                sendBroadcast(intent);
//                //Log.d(TAG, "SENT broadcast " + intent.toString());
//                Log.d(TAG, "SENT ELP broadcast = " + new String(ELP_data));
//            }
//        };
//        final ScheduledFuture<?> timerHandle =
//                scheduler.scheduleWithFixedDelay(sendTime, 10, 60, SECONDS);
//        scheduler.schedule(new Runnable() {
//            public void run() { timerHandle.cancel(true); }
//        }, 60 * 60, SECONDS);
//    }

}
