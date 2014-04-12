package com.example.mobilesmalllibrary;

import java.security.MessageDigest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Generic {
	public static final int scan_REQUEST = 1;
	public static final int signIn = 2;
	
	public static String serverurl = "http://piguloming.no-ip.org:90/api/";
	public static String GoogleAPIGenerateQRUrl = "http://chart.apis.google.com/chart?cht=qr&chl=content&chs=200x200";
	public static String loginToken = "0";
	public static String LID = "";
	public static String LEmail = "";
	public static final String GOOGLE_PROJECT_ID = "1007963483160";
	public static final String sharedPreferenceName = "SP";
	// Shared Preferences keys
	public static final String notificationStatusAnnouncement = "notificationStatusAnnouncement";
	public static final String notificationStatusExpire = "notificationStatusExpire reminder";
	public static final String notificationStatusReservation = "notificationStatusReservation reminder";
	public static final String REG_ID = "regId";
	public static final String APP_VERSION = "appVersion";
	public static final String GCM_ID = "gcmId";
	
	public static String computeHash(String input) {
		StringBuffer sb = new StringBuffer();
		try
		{
		    MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    digest.reset();
	
		    byte[] byteData = digest.digest(input.getBytes("UTF-8"));

		    for (int i = 0; i < byteData.length; i++){
		      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		    }
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	    return sb.toString();
	}
	
	public static boolean isOnline(Activity a) {
	    ConnectivityManager cm = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
}
