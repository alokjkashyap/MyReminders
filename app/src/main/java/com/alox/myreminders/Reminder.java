package com.alox.myreminders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class Reminder extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MyReminders";
    SharedPreferences mPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        mPreferences = context.getSharedPreferences("rems",Context.MODE_PRIVATE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(intent.getStringExtra("title")+"")
                .setContentText("Reminder by My Reminders")
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        //UerRequest Code to identify
        String rem = intent.getStringExtra("rem")+"";
        Log.e("intent",rem);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification bNotification = builder.build();
        NotificationManager NotifMan  = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotifMan.notify((int) System.currentTimeMillis(),bNotification);
        deleteRem(rem);
        MainActivity.notified.setValue(rem);
    }

    private void deleteRem(String remString) {
        Set<String> rem = mPreferences.getStringSet("RecentRems",null);
        if (rem!=null) {
            rem.remove(remString);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putStringSet("RecentRems",rem);
            editor.apply();
        }
    }
}
