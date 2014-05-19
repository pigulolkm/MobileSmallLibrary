package com.example.mobilesmalllibrary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ExpireAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getExtras();
		String msg = 	"Title : " + bundle.getString("title")+ "\n"
						+ "Author : "+ bundle.getString("author")+ "\n"
						+ "Publisher : "+ bundle.getString("publisher") + "\n"
						+ "You should return the book before " + bundle.getString("shouldReturnedDate") +".";
		int NOTIFICATION_ID = Integer.parseInt(bundle.getString("Bid"));
		
		Log.d("ExpireAlarm Received", "Preparing to send notification... : "+NOTIFICATION_ID);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);// set intent so it does not start a new activity
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_ONE_SHOT); // set PendingIntent flag that is does not start a new activity
		
		Notification notification = new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.launcher)
						.setContentTitle("Expiry Reminder")
						.setContentText(msg)
						.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
						.setDefaults(Notification.DEFAULT_ALL)
						.setAutoCancel(true)
						.setContentIntent(contentIntent)
						.setPriority(Notification.PRIORITY_MAX)
						.build();
		
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		Log.d("ExpireAlarmReceiver", "Notification sent sucessfully");
	}
}
