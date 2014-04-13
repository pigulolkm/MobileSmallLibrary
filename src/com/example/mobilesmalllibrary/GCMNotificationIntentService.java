package com.example.mobilesmalllibrary;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMNotificationIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	
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
				
				sendNotification("Message Received from Google GCM Server: " + extras.get(Generic.MESSAGE_KEY));
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(String msg)
	{
		Log.d(TAG, "Preparing to send notification... : " + msg);
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		
		builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.launcher)
					.setContentTitle("Mobile Small Library")
					.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
					.setContentText(msg);
		
		builder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
		Log.d(TAG, "Notification sent sucessfully");
	}
}
