package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ReservationActivity extends Activity {

	private ListView ListViewAllReservation;
	private ActionMode mActionMode;
	private int selectedItemId;
	private ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	private SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reservation);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		findViews();
		showReservations();
		setListener();
	}
	
	public void findViews()
	{
		ListViewAllReservation = (ListView)findViewById(R.id.ListViewAllReservation);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setListener()
	{
		ListViewAllReservation.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				selectedItemId = pos;
			}
		});
		
		ListViewAllReservation.setOnItemLongClickListener(new OnItemLongClickListener(){
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
				if(v.isActivated())
				{
					v.setActivated(false);
					selectedItemId = -1;
					//ListViewAllReservation.setItemChecked(pos, false);
				}
				else
				{
					// Start the CAB using the ActionMode.Callback defined above
			        mActionMode = ReservationActivity.this.startActionMode(mActionModeCallback);
					v.setActivated(true);
					//ListViewAllReservation.setItemChecked(pos, true);
					selectedItemId = pos;
					
				}
				return false;
			}
		});
	}
	
	public void showReservations()
	{
		String url = Generic.serverurl + "Reservation/GetReservationByUser/" + Generic.LID + "?token=" + Generic.loginToken;
		
		if(checkNetworkState())
		{
			new GeReservationOperation().execute(url);
		}
	}
	
	private class GeReservationOperation extends AsyncTask<String, Void, String>{
		
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(ReservationActivity.this);
		
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
			Log.d("Reservation", result);
			ShowReservationListView(result);
		}
	}
	
	private void ShowReservationListView(String result)
	{
		try {
			HashMap<String,Object> item;
			JSONObject json = new JSONObject(result);
			JSONArray jsonArray = json.getJSONArray("Reservations");
			JSONObject jsonObj;
			JSONObject book;
			JSONObject reservation;
			
			for(int i = 0; i < jsonArray.length(); i++)
			{
				jsonObj = jsonArray.getJSONObject(i);
				book = jsonObj.getJSONObject("Book");
				reservation = jsonObj.getJSONObject("Reservation");
				
				item = new HashMap<String,Object>();
				
				// B_id
				item.put("B_id", book.getString("B_id"));
				
				// B_title
				item.put("B_title", book.getString("B_title"));
				
				// B_author
				item.put("B_author", book.getString("B_author"));
				
				// B_publisher
				item.put("B_publisher", book.getString("B_publisher"));
				
				// R_id, L_id, R_datetime
				item.put("R_id", reservation.getString("R_id"));
				item.put("L_id", reservation.getString("L_id"));
				item.put("datetime", reservation.getString("R_datetime"));
				
				// R_datetime
				String[] datetime = reservation.getString("R_datetime").split("T");
				item.put("R_datetime", "Reserved Date : "+ datetime[0]);
				
				// R_getBookDate
				item.put("getBookDate", reservation.getString("R_getBookDate"));
				if(!reservation.isNull("R_getBookDate"))
				{
					String[] getBookDate = reservation.getString("R_getBookDate").split("T");
					item.put("R_getBookDate", "Got book deadline : " + getBookDate[0]);
				}
				else
				{
					item.put("R_getBookDate", "Got book deadline : N/A");
				}
				
				// R_finishDatetime
				if(!reservation.isNull("R_finishDatetime"))
				{
					String finishDatetime = reservation.getString("R_finishDatetime").replace("T", " ");
					String[] temp = finishDatetime.split(":");
					String YYYYmmddHHMM = temp[0] + ":" + temp[1];
					item.put("R_finishDatetime", "Finished on : " + YYYYmmddHHMM);
				}
				else
				{
					item.put("R_finishDatetime", "Finished on : N/A");
				}
				
				// R_isActivated
				if(reservation.getString("R_isActivated").equals("true"))
				{
					item.put("R_isActivated", "Active");
				}
				else
				{
					item.put("R_isActivated", "InActive");
				}
				
				list.add(item);
			}
			
			adapter = new SimpleAdapter(ReservationActivity.this, list, R.layout.listview_reservation_book_item, 
					new String[]{"B_title","B_author","B_publisher","R_datetime", "R_getBookDate", "R_finishDatetime", "R_isActivated"},
					new int[]{R.id.textViewReservationTitle, R.id.textViewReservationAuthor, R.id.textViewReservationPublisher, R.id.textViewReservationDateTime, R.id.textViewReservationGetBookDateTime, R.id.textViewReservationFinishDateTime, R.id.textViewReservationState});
			
			ListViewAllReservation.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private class CancelReservation extends AsyncTask<String, Void, String>{
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(ReservationActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(false);
			Dialog.setCanceledOnTouchOutside(false);
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
				HttpEntity se = new StringEntity(params[1], HTTP.UTF_8);
				httpPut.setEntity(se);
				httpPut.setHeader("Content-Encoding", "UTF-8");
				httpPut.setHeader("Content-Type", "application/json");
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
			Log.d("Cancel Reservation Result", result);
			CancelReservationPostExecute(result);
		}
	}
	
	private void CancelReservationPostExecute(String result)
	{
		try 
		{
			JSONObject jsonObj = new JSONObject(result);
			AlertDialog.Builder builder = new AlertDialog.Builder(ReservationActivity.this);
			
			if(jsonObj.getBoolean("Result") == true)
			{
				builder.setTitle("Accepted");
				builder.setMessage(jsonObj.getString("Message"));
				builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showReservations();
						list.clear();
					}
				});
				builder.create().show();
			}
			else
			{
				builder.setTitle("Rejected");
				builder.setMessage(jsonObj.getString("Message"));
				builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.reservation, menu);
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
			intent.setClass(ReservationActivity.this,MainActivity.class);
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
			ListViewAllReservation.setItemChecked(-1, true);
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.show_reservation_on_listview_item_selected, menu);
	        return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
	            case R.id.action_renew:
	                HashMap<String,Object> bookItem = list.get(ListViewAllReservation.getCheckedItemPosition());
	                String url = Generic.serverurl + "Reservation/PutReservation?token=" + Generic.loginToken;
	                JSONObject jsonObj = new JSONObject();
	                try 
	                {
						jsonObj.put("R_id", bookItem.get("R_id").toString());
						jsonObj.put("L_id", bookItem.get("L_id").toString());
						jsonObj.put("B_id", bookItem.get("B_id").toString());
						jsonObj.put("R_datetime", bookItem.get("datetime").toString());
						if(bookItem.get("getBookDate") == null)
						{
							jsonObj.put("getBookDate", JSONObject.NULL);
						}
						else
						{
							jsonObj.put("getBookDate", bookItem.get("getBookDate").toString());
						}
					} 
	                catch (JSONException e) 
	                {
						e.printStackTrace();
					}
	                
	                if(checkNetworkState())
	                {
	                	Log.d("Cancel reservation", url+"\n"+jsonObj.toString());
	                	new CancelReservation().execute(url, jsonObj.toString());
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
