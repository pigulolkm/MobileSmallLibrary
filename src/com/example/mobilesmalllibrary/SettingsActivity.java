package com.example.mobilesmalllibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class SettingsActivity extends Activity {
	
	private ListView ListViewNotificationSetting;
	
	private SharedPreferences spref;
	public static final String TAG = "GCMNotificationService";
	
	GoogleCloudMessaging gcm;
	String regId;
	Context context;
	
	
	private String[] notificationSetting = new String[]
	{
		"Announcement",
		"Expire reminder",
		"Reservation reminder"
	};
	
	private Boolean[] notificationStatus = {
		true, false, false
	};
	
	private SimpleAdapter notificationAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		context = getApplicationContext();
		
		findViews();
		init();
		setListener();
	}
	
	private void findViews()
	{
		ListViewNotificationSetting = (ListView)findViewById(R.id.ListViewNotificationSetting);
	}
	
	private void init()
	{
		spref = getApplication().getSharedPreferences(Generic.sharedPreferenceName, Context.MODE_PRIVATE);
		// Set adapter of ListViewNotificationSetting
		List<HashMap<String,Object>> notificationList = new ArrayList<HashMap<String,Object>>();

		for(int i=0; i < notificationSetting.length; i++){
            HashMap<String, Object> hm = new HashMap<String,Object>();
            notificationStatus[i] = spref.getBoolean("notificationStatus"+notificationSetting[i], true);
            hm.put("txt", notificationSetting[i]);
            hm.put("stat",notificationStatus[i]);
            notificationList.add(hm);
        }
		
		notificationAdapter = new SimpleAdapter(SettingsActivity.this, notificationList, R.layout.listview_push_notification_setting_item,
				new String[]{ "txt", "stat" }, new int[]{ R.id.tv_item, R.id.switch_status });
		
		ListViewNotificationSetting.setAdapter(notificationAdapter);
	}
	
	private void setListener()
	{
		ListViewNotificationSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> lv, View item, int position, long id) 
			{
				ListView listView = (ListView) lv;
				 
                SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();
 
                HashMap<String,Object> hm = (HashMap) adapter.getItem(position);
 
                /** The clicked Item in the ListView */
                RelativeLayout rLayout = (RelativeLayout) item;
 
                /** Getting the toggle button corresponding to the clicked item */
                Switch sb = (Switch) rLayout.getChildAt(1);
 
                if(sb.isChecked()){
                    sb.setChecked(false);
                    notificationStatus[position] = false;
                }else{
                    sb.setChecked(true);
                    notificationStatus[position] = true;
                    
                    registerGCM();
                }
                
                // Store the status to Shared Preferences
                SharedPreferences.Editor editor = spref.edit();
                editor.putBoolean("notificationStatus"+(String) hm.get("txt"), notificationStatus[position]);
                editor.apply();
                editor.commit();
                
                Log.d("Notification Settings", (String)hm.get("txt") + " is " + notificationStatus[position]);
			}
		});
	}
	
	public void registerGCM()
	{
		gcm = GoogleCloudMessaging.getInstance(this);
		regId = getRegistrationId(context);
		if (TextUtils.isEmpty(regId)) { 
		      registerInBackground();
		}
	}
	
	private String getRegistrationId(Context context)
	{
		final SharedPreferences prefs = getSharedPreferences(Generic.sharedPreferenceName, Context.MODE_PRIVATE);
		String registrationId = prefs.getString(Generic.REG_ID, "");
		if (registrationId.isEmpty()) 
		{
			Log.i(TAG, "Registration ID not found.");
			return "";
		}
		else
		{
			Log.i(TAG, "Registration ID is found: " + registrationId);
		}
		
	    int registeredVersion = prefs.getInt(Generic.APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    
	    if (registeredVersion != currentVersion) 
	    {
	      Log.i(TAG, "App version changed.");
	      return "";
	    }
		return registrationId;
	}
	
	private static int getAppVersion(Context context) {
	    try 
	    {
	    	PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	    	return packageInfo.versionCode;
	    } 
	    catch (NameNotFoundException e) 
	    {
	    	Log.d("RegisterActivity", "I never expected this! Going down, going down!" + e);
	    	throw new RuntimeException(e);
	    }
	}
	
	private void registerInBackground() 
	{
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) 
			{
				String msg = "";
				try 
				{
					if (gcm == null) 
					{
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regId = gcm.register(Generic.GOOGLE_PROJECT_ID);

				} 
				catch (IOException ex) 
				{
			          msg = "Error :" + ex.getMessage();
			          Log.d("SettingsActivity", "Error: " + msg);
				}
				Log.d("SettingsActivity", "AsyncTask completed. " + msg);
			    return regId;
			}
		 
		    @Override
		    protected void onPostExecute(String regId) 
		    {
		    	Log.d(TAG, "Registered with GCM Server. RegID : " + regId);
		    	storeRegistrationId(context, regId);
		    }
		}.execute(null, null, null);
	}
	
	private void storeRegistrationId(Context context, String regId) 
	{
	    final SharedPreferences prefs = getSharedPreferences(Generic.sharedPreferenceName, Context.MODE_PRIVATE);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(Generic.REG_ID, regId);
	    editor.putInt(Generic.APP_VERSION, appVersion);
	    editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
