package com.example.mobilesmalllibrary;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.mobilesmalllibrary.Generic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchBooksActivity extends Activity {

	private EditText editTextSearchKey;
	private Spinner spinnerSeachOption;
	private EditText editTextSearchKey2;
	private Spinner spinnerSeachOption2;
	private EditText editTextSearchKey3;
	private Spinner spinnerSeachOption3;
	
	private String[] key;
    private ArrayAdapter<String> searchOptionAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_books);
		 	    
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	    init();
        findViews();
        setListener();
        
        spinnerSeachOption.setAdapter(searchOptionAdapter);
        spinnerSeachOption2.setAdapter(searchOptionAdapter);
        spinnerSeachOption3.setAdapter(searchOptionAdapter);
	}
	
	private void init() {
		key 				= new String[]{"Author","Title","Subject","Publisher","ISBN","Scan Code"};
		searchOptionAdapter 	= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, key);
		
	}
	
	private void findViews(){
		editTextSearchKey = (EditText)findViewById(R.id.editTextSearchKey);
		editTextSearchKey2 = (EditText)findViewById(R.id.editTextSearchKey2);
		editTextSearchKey3 = (EditText)findViewById(R.id.editTextSearchKey3);
		spinnerSeachOption = (Spinner)findViewById(R.id.spinnerSeachOption);
		spinnerSeachOption2 = (Spinner)findViewById(R.id.spinnerSeachOption2);
		spinnerSeachOption3 = (Spinner)findViewById(R.id.spinnerSeachOption3);
	}
	
	private void setListener() {
		
		spinnerSeachOption.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				///////////////////////////////////////////
				// Handle Case: Scan code, call scanner // 
				/////////////////////////////////////////
				if(arg0.getSelectedItem().toString().equals("Scan Code"))
				{
					if(isCameraAvailable())
					{
						Intent intent = new Intent();
						intent.setClass(SearchBooksActivity.this, CameraTestActivity.class);
						startActivityForResult(intent, Generic.scan_REQUEST);
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
	    });
		
		spinnerSeachOption2.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				///////////////////////////////////////////
				// Handle Case: Scan code, call scanner // 
				/////////////////////////////////////////
				if(arg0.getSelectedItem().toString().equals("Scan Code"))
				{
					if(isCameraAvailable())
					{
						Intent intent = new Intent();
						intent.setClass(SearchBooksActivity.this, CameraTestActivity.class);
						startActivityForResult(intent, Generic.scan_REQUEST+1);
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
	    });
		
		spinnerSeachOption3.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				///////////////////////////////////////////
				// Handle Case: Scan code, call scanner // 
				/////////////////////////////////////////
				if(arg0.getSelectedItem().toString().equals("Scan Code"))
				{
					if(isCameraAvailable())
					{
						Intent intent = new Intent();
						intent.setClass(SearchBooksActivity.this, CameraTestActivity.class);
						startActivityForResult(intent, Generic.scan_REQUEST+2);
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
	    });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case Generic.scan_REQUEST:
				if(resultCode == RESULT_OK)
				{
					editTextSearchKey.setText(data.getStringExtra("SCAN_RESULT"));
                } 
                break;
			case Generic.scan_REQUEST+1:
				if(resultCode == RESULT_OK)
				{
					editTextSearchKey2.setText(data.getStringExtra("SCAN_RESULT"));
                } 
                break;
			case Generic.scan_REQUEST+2:
				if(resultCode == RESULT_OK)
				{
					editTextSearchKey3.setText(data.getStringExtra("SCAN_RESULT"));
                } 
                break;
		}
	}
	
	//////////////////////////////////////////////////////
	// SearchBooks button click, success: make request //
	////////////////////////////////////////////////////
	public void searchBooks(View v) {
		if(editTextSearchKey.getText().toString().trim().equals(""))
		{
			Toast.makeText(SearchBooksActivity.this, "Please enter search terms", Toast.LENGTH_LONG).show();
		}
		else
		{
			String searchKey = editTextSearchKey.getText().toString();
			String searchKey2 = editTextSearchKey2.getText().toString();
			String searchKey3 = editTextSearchKey3.getText().toString();
			String searchOption = spinnerSeachOption.getSelectedItem().toString();
			String searchOption2 = spinnerSeachOption2.getSelectedItem().toString();
			String searchOption3 = spinnerSeachOption3.getSelectedItem().toString();
			// Remove the space of Scan Code
			if(searchOption.equals("Scan Code") || searchOption2.equals("Scan Code") || searchOption3.equals("Scan Code"))
			{
				searchOption = "ScanCode";
				searchOption2 = "ScanCode";
				searchOption3 = "ScanCode";
			}
			String url = Generic.serverurl + "Book/PostGetBookByKey";
			JSONObject json = new JSONObject();
			try 
			{
				json.put( "searchKey" , searchKey);
				json.put( "searchKey2" , searchKey2);
				json.put( "searchKey3" , searchKey3);
				json.put( "searchOption" , searchOption);
				json.put( "searchOption2" , searchOption2);
				json.put( "searchOption3" , searchOption3);
				
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			
			if(checkNetworkState())
			{
				new GetSearchBooksOperation().execute(url, json.toString());
			}
		}
	}
	
	/////////////////////////
	// Reset button click //
	///////////////////////
	public void reset(View v) {
		editTextSearchKey.setText("");
		editTextSearchKey2.setText("");
		editTextSearchKey3.setText("");
	}
	
	private class GetSearchBooksOperation extends AsyncTask<String, Void, String>{
		
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(SearchBooksActivity.this);
		
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
				HttpPost httpPost = new HttpPost(params[0]);
				StringEntity se= new StringEntity(params[1], HTTP.UTF_8);
				// Set httpPost Entity
				httpPost.setEntity(se);
				// Set some headers to inform server about the type of the content
				httpPost.setHeader("Content-Encoding", "UTF-8");
				httpPost.setHeader("Content-Type", "application/json");
				HttpResponse httpResponse = client.execute(httpPost);
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
			Log.d("SearchBooksActivity", result+"");
			Dialog.dismiss();
			Intent intent = new Intent(SearchBooksActivity.this, ShowSearchBooksResultActivity.class);
			intent.putExtra("SearchBooksResult", result);
			startActivity(intent);
		}
	}
	
	public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.search_books, menu);
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
			intent.setClass(SearchBooksActivity.this,MainActivity.class);
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
				}
        	}).create().show();;
        }
		return false;
	}
}
