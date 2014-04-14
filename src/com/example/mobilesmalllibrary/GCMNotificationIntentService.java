package com.example.mobilesmalllibrary;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMNotificationIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	Notification notification;
	
	private static final String TAG = "GCMNotificationIntentService";
	
	public GCMNotificationIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		
		if(!extras.isEmpty())
		{
			if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
			{
				sendNotification("Send error: " + extras.toString());
			}
			else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
			{
				sendNotification("Deleted message on server: " + extras.toString());
			}
			else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
			{
				for(int i = 0; i < 3; i++)
				{
					Log.i(TAG, "Working..." + (i + 1) + "/5 @ " + SystemClock.elapsedRealtime());
					try
					{
						Thread.sleep(5000);
					}
					catch(InterruptedException e)
					{	
					}
				}
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				
				sendNotification(extras.get(Generic.MESSAGE_KEY).toString());
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(String msg)
	{
		Log.d(TAG, "Preparing to send notification... : " + msg);
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);// set intent so it does not start a new activity
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT); // set PendingIntent flag that is does not start a new activity
		
		notification = new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.launcher)
						.setContentTitle("Mobile Small Library")
						.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
						.setContentText(msg)
						.setDefaults(Notification.DEFAULT_ALL)
						.setAutoCancel(true)
						.setContentIntent(contentIntent)
						.build();
		
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		Log.d(TAG, "Notification sent sucessfully");
	}
}
