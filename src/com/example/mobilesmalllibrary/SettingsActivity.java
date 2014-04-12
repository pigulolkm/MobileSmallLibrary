package com.example.mobilesmalllibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	private ProgressDialog Dialog;
	
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
 
                if(sb.isChecked())
                {
                	boolean availableToRemove = unregisterGCM();
                    sb.setChecked(availableToRemove);
                    notificationStatus[position] = availableToRemove;
                }
                else
                {
                	boolean availableToActivate = registerGCM();
                    sb.setChecked(availableToActivate);
                    notificationStatus[position] = availableToActivate; 
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
	
	public boolean registerGCM()
	{
		if(checkNetworkState())
		{
			gcm = GoogleCloudMessaging.getInstance(this);
			regId = getRegistrationId(context);
			if (TextUtils.isEmpty(regId)) { 
			      registerInBackground();
			}
			return true;
		}
		return false;
	}
	
	public boolean unregisterGCM()
	{
		if(checkNetworkState())
		{
			gcm = GoogleCloudMessaging.getInstance(this);
			String gcmId = spref.getString(Generic.GCM_ID, "-1");
			
			if(!gcmId.equals("-1"))
			{
				Log.i(TAG, "Unregister GCM start --- remove in server and preference");
				new removeRegistionIdToServer().execute(gcmId);
				removeRegistrationId(this); 
			}
			else
			{
				Log.i(TAG, "Unregister GCM start --- fail, gcmId does not exist in preference");
			}
			return false;
		}
		return true;
	}
	
	private String getRegistrationId(Context context)
	{
		String registrationId = spref.getString(Generic.REG_ID, "");
		if (registrationId.isEmpty()) 
		{
			Log.i(TAG, "Registration ID not found.");
			return "";
		}
		else
		{
			Log.i(TAG, "Registration ID is found: " + registrationId);
		}
		
	    int registeredVersion = spref.getInt(Generic.APP_VERSION, Integer.MIN_VALUE);
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
		Dialog = new ProgressDialog(SettingsActivity.this);
		Dialog.setCancelable(false);
		Dialog.setCanceledOnTouchOutside(false);
		Dialog.setTitle("Loading");
		Dialog.setMessage("Please wait...");
		Dialog.show();
		
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
		    	
		    	JSONObject jsonObj = new JSONObject();
		    	try 
		    	{
					jsonObj.put("Gcm_regID", regId);
					jsonObj.put("Gcm_userID", Generic.LID);
				} 
		    	catch (JSONException e) 
				{
					e.printStackTrace();
				}
		    	
		    	new storeRegistionIdToServer().execute(jsonObj);
		    }	
		}.execute(null, null, null);
	}
	
	private void storeRegistrationId(Context context, String regId) 
	{
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = spref.edit();
	    editor.putString(Generic.REG_ID, regId);
	    editor.putInt(Generic.APP_VERSION, appVersion);
	    editor.commit();
	}
	
	private class storeRegistionIdToServer extends AsyncTask<JSONObject, Void, String[]>
	{
		private final HttpClient  client = new DefaultHttpClient();
		
		
		@Override
		protected void onPreExecute() {
			Log.i("SettingsActivity", "Start storing regId to server");
		}
		@Override
		protected String[] doInBackground(JSONObject... jsonObj) {
			String[] result = null;
			try
			{
				HttpPost httpPost = new HttpPost(Generic.serverurl + "GCM/PostGCM");
				// Convert JSONObject to JSON to String
				String json = jsonObj[0].toString();
				// Set json to StringEntity
				StringEntity se= new StringEntity(json);
				// Set httpPost Entity
				httpPost.setEntity(se);
				// Set some headers to inform server about the type of the content
				httpPost.setHeader("Content-Encoding", "UTF-8");
				httpPost.setHeader("Content-Type", "application/json");
				HttpResponse httpResponse = client.execute(httpPost);
				
				if(httpResponse.getStatusLine().getStatusCode() == 201)
				{
					result = new String[]{ "201", EntityUtils.toString(httpResponse.getEntity()) };
				}
			}
			catch(Exception e)
			{
				
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String[] result)
		{
			Dialog.dismiss();
			if(result[0].equals("201"))
			{
				Log.i(TAG, "Success Registry GCM with GCM_ID : " + result[1]);
			    SharedPreferences.Editor editor = spref.edit();
			    editor.putString(Generic.GCM_ID, result[1]);
			    editor.commit();
			}
		}
		
	}
	
	private void removeRegistrationId(Context context) 
	{
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Removing regId on app version " + appVersion);
	    SharedPreferences.Editor editor = spref.edit();
	    editor.remove(Generic.REG_ID);
	    editor.remove(Generic.APP_VERSION);
	    editor.remove(Generic.GCM_ID);
	    editor.commit();
	}
	
	private class removeRegistionIdToServer extends AsyncTask<String, Void, String>
	{
		private final HttpClient client = new DefaultHttpClient();
		
		@Override
		protected void onPreExecute() {
			Dialog = new ProgressDialog(SettingsActivity.this);
			Dialog.setCancelable(true);
			Dialog.setCanceledOnTouchOutside(false);
			Dialog.setTitle("Loading");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		@Override
		protected String doInBackground(String... gcmID) {
			String result = null;
			try
			{
				HttpDelete httpDelete = new HttpDelete(Generic.serverurl + "GCM/DeleteGCM/" + gcmID[0]);
				HttpResponse httpResponse = client.execute(httpDelete);
				
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{
					result = "200";
				}
				else
				{
					result = httpResponse.getStatusLine().toString();
					result += " "+ EntityUtils.toString(httpResponse.getEntity());
				}
				
			}
			catch(Exception e)
			{
				
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			Dialog.dismiss();
			if(result.equals("200"))
			{
				Log.i(TAG, "Success unregistry GCM");
			}
		}
		
	}
	
	private boolean checkNetworkState()
	{
		if(Generic.isOnline(this))
		{
			return true;
		}
		else
        {
        	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        	dialog.setTitle("Warning");
        	dialog.setMessage(getResources().getString(R.string.warning_networkConnectionError));
        	dialog.setNeutralButton("OK", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
        	}).create().show();;
        }
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
