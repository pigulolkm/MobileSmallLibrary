package com.example.mobilesmalllibrary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ShowBorrowingRecordActivity extends Activity {
	
	private ListView listViewBorrowingRecordResult;
	private TextView textViewBorrowingAmount;
	private TextView textViewNonReturnedAmount;
	private ActionMode mActionMode;
	private int selectedItemId;
	private ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	private SimpleAdapter adapter;
	private SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_borrowing_record);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		findViews();
		ShowBorrowingRecord();
		setListener();
	}
	
	private void findViews()
	{
		listViewBorrowingRecordResult = (ListView)findViewById(R.id.listViewBorrowingRecordResult);
		textViewBorrowingAmount = (TextView)findViewById(R.id.textViewBorrowedAmount);
		textViewNonReturnedAmount = (TextView)findViewById(R.id.textViewNonReturnedAmount);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setListener()
	{
		listViewBorrowingRecordResult.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				selectedItemId = pos;
			}
		});
		
		listViewBorrowingRecordResult.setOnItemLongClickListener(new OnItemLongClickListener(){
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
				if(v.isActivated())
				{
					v.setActivated(false);
					selectedItemId = -1;
					//listViewBorrowingRecordResult.setItemChecked(pos, false);
				}
				else
				{
					// Start the CAB using the ActionMode.Callback defined above
			        mActionMode = ShowBorrowingRecordActivity.this.startActionMode(mActionModeCallback);
					v.setActivated(true);
					//listViewBorrowingRecordResult.setItemChecked(pos, true);
					selectedItemId = pos;
					
				}
				return false;
			}
		});
	}

	private void ShowBorrowingRecord()
	{
		String url = Generic.serverurl + "BorrowingRecord/GetBorrowingRecord/" + Generic.LID + "?token=" + Generic.loginToken;
		
		if(checkNetworkState())
		{
			new GetBorrowingRecordOperation().execute(url);
		}
	}
	
	private class GetBorrowingRecordOperation extends AsyncTask<String, Void, String>{
		
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(ShowBorrowingRecordActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(true);
			Dialog.setTitle("Loading");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			String result = null;
			try
			{
				HttpGet httpGet = new HttpGet(params[0]);
				HttpResponse httpResponse = client.execute(httpGet);
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{
					result = EntityUtils.toString(httpResponse.getEntity());
					
				}
				else
				{
					result = httpResponse.getStatusLine().toString();
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

			ShowBorrowingRecordListView(result);
			SetExpireReminder(result);
		}
	}
	
	private void ShowBorrowingRecordListView(String result)
	{
		try {
			HashMap<String,Object> item;
			JSONObject json = new JSONObject(result);
			JSONArray jsonArray = json.getJSONArray("BorrowingRecord");
			JSONObject jsonObj;
			
			for(int i = 0; i < jsonArray.length(); i++)
			{
				jsonObj = jsonArray.getJSONObject(i);
				item = new HashMap<String,Object>();

				item.put("Bid", jsonObj.getString("Bid"));
				item.put("title", jsonObj.getString("Title"));
				item.put("author", jsonObj.getString("Author"));
				item.put("publisher", jsonObj.getString("Publisher"));
				
				String[] datetime = jsonObj.getString("ShouldReturnedDate").split("T");
				item.put("shouldReturnedDate", "Should return on : "+datetime[0]);
				if(jsonObj.getString("ReturnedDate").equals("null"))
				{
					item.put("ReturnedState", "Not Returned");
				}
				else
				{
					item.put("ReturnedState", "Returned");
				}
				
				list.add(item);
			}
			
			textViewBorrowingAmount.setText("Borrowed Amount : " + json.getString("BorrowedAmount"));
			textViewNonReturnedAmount.setText("Non-returned Amount : " + json.getString("NonReturnedAmount"));
			
			adapter = new SimpleAdapter(ShowBorrowingRecordActivity.this, list, R.layout.listview_borrowing_record_book_item, 
					new String[]{"title","author","publisher","shouldReturnedDate", "ReturnedState"},
					new int[]{R.id.textViewBorrowingRecordTitle, R.id.textViewBorrowingRecordAuthor, R.id.textViewBorrowingRecordPublisher, R.id.textViewBorrowingRecordShouldReturnedDate, R.id.textViewBorrowingRecordReturnedState});
			
			listViewBorrowingRecordResult.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SetExpireReminder(String result)
	{
		// skip create DB for removing the alarm by records
		// db = Generic.createDatabase(getApplicationContext());
		try 
		{
			JSONObject json = new JSONObject(result);
			JSONArray jsonArray = json.getJSONArray("NonReturnedRecord");
			JSONObject jsonObj;
			
			for(int i = 0; i < jsonArray.length(); i++)
			{
				jsonObj = jsonArray.getJSONObject(i);
				// TODO add record to DB for removing the alarm by records -- skip
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = format.parse(jsonObj.getString("ShouldReturnedDate").split("T")[0]);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY, 12);
				calendar.add(calendar.DAY_OF_MONTH, -1);
				Log.d("SetExpireReminder", String.valueOf(calendar.get(Calendar.DATE))+"-"+String.valueOf(calendar.get(Calendar.MONTH)+1)+"-"+String.valueOf(calendar.get(Calendar.YEAR)));
				Log.d("SetExpireReminder", calendar.getTime().toString());
				
				Intent intent = new Intent(this, ExpireAlarmReceiver.class);
				intent.setAction("Expire_Alarm_Alert");
				intent.putExtra("Bid", jsonObj.getString("Bid"));
				intent.putExtra("title", jsonObj.getString("Title"));
				intent.putExtra("author", jsonObj.getString("Author"));
				intent.putExtra("publisher", jsonObj.getString("Publisher"));
				intent.putExtra("shouldReturnedDate", jsonObj.getString("ShouldReturnedDate").split("T")[0]);
			
				PendingIntent pi = PendingIntent.getBroadcast(this, Integer.parseInt(jsonObj.getString("Bid")), intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private class RenewBooksOperation extends AsyncTask<String, Void, String>{
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(ShowBorrowingRecordActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(false);
			Dialog.setTitle("Loading");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			String result = null;
			try
			{
				HttpPut httpPut = new HttpPut(params[0]);
				HttpResponse httpResponse = client.execute(httpPut);
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{
					result = EntityUtils.toString(httpResponse.getEntity());
					
				}
				else
				{
					result = httpResponse.getStatusLine().toString();
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
			RenewBooksPostExecute(result);
		}
	}
	
	private void RenewBooksPostExecute(String result)
	{
		try {
			JSONObject jsonObj = new JSONObject(result);
			
			if(jsonObj.getString("Result").equals("True"))
			{
				ShowBorrowingRecord();
				list.clear();
			}
			else // Result : False
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(ShowBorrowingRecordActivity.this);
				dialog.setMessage(jsonObj.getString("Message"));
				dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.create().show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.show_borrowing_record, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {   
		switch(menuItem.getItemId())
		{
	    	case android.R.id.home:
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setClass(ShowBorrowingRecordActivity.this,MainActivity.class);
	        startActivity(intent); 
	        return true;
		}
	    return false;
    }
	
	@SuppressLint("NewApi")
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		// Invoke when Tick is click & ActionMode.finish()
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			listViewBorrowingRecordResult.setItemChecked(-1, true);
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.show_borrowing_record_on_listview_item_selected, menu);
	        return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
	            case R.id.action_renew:
	                HashMap<String,Object> bookItem = list.get(listViewBorrowingRecordResult.getCheckedItemPosition());
	                String url = Generic.serverurl + "BorrowingRecord/PutBorrowingRecordRenewBooks/" + bookItem.get("Bid").toString();
	                
	                if(checkNetworkState())
	                {
	                	new RenewBooksOperation().execute(url);
	                }
	                
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
			}

		}
	};
	
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
					finish();
				}
        	}).create().show();;
        }
		return false;
	}
}
