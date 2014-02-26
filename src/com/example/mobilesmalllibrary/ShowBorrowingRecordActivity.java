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
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_borrowing_record);
		
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
		//listViewBorrowingRecordResult.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);		
		listViewBorrowingRecordResult.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,	int pos, long id) {
				if(v.isActivated())
				{
					v.setActivated(false);
				}
				else
				{
					v.setActivated(true);
				}
				return false;
			}
		});
	}
	
	private void ShowBorrowingRecord()
	{
		String url = Generic.serverurl + "BorrowingRecord/GetBorrowingRecord/" + Generic.LID + "?token=" + Generic.loginToken;
		
		new GetBorrowingRecordOperation().execute(url);
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
		}
	}
	
	private void ShowBorrowingRecordListView(String result)
	{
		try {
			ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
			HashMap<String,Object> item;
			JSONObject json = new JSONObject(result);
			JSONArray jsonArray = json.getJSONArray("BorrowingRecord");
			JSONObject jsonObj;
			
			for(int i = 0; i < jsonArray.length(); i++)
			{
				jsonObj = jsonArray.getJSONObject(i);
				item = new HashMap<String,Object>();
				
				item.put("title", jsonObj.getString("Title"));
				item.put("author", jsonObj.getString("Author"));
				item.put("publisher", jsonObj.getString("Publisher"));
				item.put("shouldReturnedDate", jsonObj.getString("ShouldReturnedDate"));
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
			
			SimpleAdapter adapter = new SimpleAdapter(ShowBorrowingRecordActivity.this, list, R.layout.listview_borrowing_record_book_item, 
					new String[]{"title","author","publisher","shouldReturnedDate", "ReturnedState"},
					new int[]{R.id.textViewBorrowingRecordTitle, R.id.textViewBorrowingRecordAuthor, R.id.textViewBorrowingRecordPublisher, R.id.textViewBorrowingRecordShouldReturnedDate, R.id.textViewBorrowingRecordReturnedState});
			
			listViewBorrowingRecordResult.setAdapter(adapter);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_borrowing_record, menu);
		return true;
	}

}
