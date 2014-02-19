package com.example.mobilesmalllibrary;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.widget.ListView;

public class ShowBorrowingRecordActivity extends Activity {
	
	private ListView listViewBorrowingRecordResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_borrowing_record);
		
		findViews();
		ShowBorrowingRecord();
	}
	
	private void findViews()
	{
		listViewBorrowingRecordResult = (ListView)findViewById(R.id.listViewBorrowingRecordResult);
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
			// TODO set Adapter to ListView
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_borrowing_record, menu);
		return true;
	}

}
