package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ReservationActivity extends Activity {

	private ListView ListViewAllReservation;
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
	}
	
	public void findViews()
	{
		ListViewAllReservation = (ListView)findViewById(R.id.ListViewAllReservation);
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

				// B_title
				item.put("B_title", book.getString("B_title"));
				
				// B_author
				item.put("B_author", book.getString("B_author"));
				
				// B_publisher
				item.put("B_publisher", book.getString("B_publisher"));
				
				// R_id
				item.put("R_id", reservation.getString("R_id"));
				
				// R_datetime
				String[] datetime = reservation.getString("R_datetime").split("T");
				item.put("R_datetime", "Reserved Date : "+ datetime[0]);
				
				// R_finishDatetime
				if(!reservation.isNull("R_finishDatetime"))
				{
					String finishDatetime = reservation.getString("R_finishDatetime").replace("T", " ");
					String[] temp = finishDatetime.split(":");
					String YYYYmmddHHMM = temp[0] + ":" + temp[1];
					item.put("R_finishDatetime", "Got book on : " + YYYYmmddHHMM);
				}
				else
				{
					item.put("R_finishDatetime", "Got book on : N/A");
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
					new String[]{"B_title","B_author","B_publisher","R_datetime", "R_finishDatetime", "R_isActivated"},
					new int[]{R.id.textViewReservationTitle, R.id.textViewReservationAuthor, R.id.textViewReservationPublisher, R.id.textViewReservationDateTime, R.id.textViewReservationFinishDateTime, R.id.textViewReservationState});
			
			ListViewAllReservation.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
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
