package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class ShowSearchBooksResultActivity extends Activity {

	private ListView listViewSearchResult;
	
	private ActionMode mActionMode;
	private int selectedItemId;
	private ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_search_books_result);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		String result = intent.getStringExtra("SearchBooksResult");
		
		findViews();
		showBookSearchedResult(result);
	}
	
	private void findViews(){
		listViewSearchResult = (ListView)findViewById(R.id.listViewSearchResult);
		listViewSearchResult.setOnItemLongClickListener(new OnItemLongClickListener(){
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
				if(v.isActivated())
				{
					v.setActivated(false);
					selectedItemId = -1;
				}
				else
				{
					if(Generic.isLoggedIn())
					{
						// Start the CAB using the ActionMode.Callback defined above
				        mActionMode = ShowSearchBooksResultActivity.this.startActionMode(mActionModeCallback);
						v.setActivated(true);
						selectedItemId = pos;
					}
				}
				return false;
			}
		});
	}
	
	public void showBookSearchedResult(String result)
	{
		try {
			HashMap<String,Object> item;
			JSONArray jsonArray = new JSONArray(result);

			if(jsonArray.length() != 0)
			{			
				JSONObject jsonObj;
				
				for(int i = 0; i < jsonArray.length(); i++)
				{
					jsonObj = jsonArray.getJSONObject(i);
					item = new HashMap<String,Object>();
					
					item.put("Bid", jsonObj.getString("B_id"));
					item.put("title", jsonObj.getString("B_title"));
					item.put("author", jsonObj.getString("B_author"));
					item.put("publisher",jsonObj.getString("B_publisher"));
					item.put("publicationDate", "Published on : "+jsonObj.getString("B_publicationDate"));
					item.put("status", jsonObj.getString("B_status"));
					
					list.add(item);
				}
				
				SimpleAdapter adapter = new SimpleAdapter(ShowSearchBooksResultActivity.this, list, R.layout.listview_book_item, 
						new String[]{"title","author","publisher","publicationDate", "status"},
						new int[]{R.id.textViewBookTitle, R.id.textViewBookAuthor, R.id.textViewBookPublisher, R.id.textViewBookPublicationDate, R.id.textViewBookStatus});
				
				listViewSearchResult.setAdapter(adapter);
			}
			else
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(ShowSearchBooksResultActivity.this);
		         dialog.setTitle("0 results.");
		         dialog.setNeutralButton("OK", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
		         });
		         dialog.create().show();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	public void reserveBook(String url, String bid)
	{
		JSONObject jsonObj = new JSONObject();
		try 
		{
			jsonObj.put("B_id", bid);
			jsonObj.put("L_id", Generic.LID);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		Log.d("ShowSearchBookResultActivity url", url);
		Log.d("jsonObj", jsonObj.toString());
		
		new AsyncTask<String, Void, String>()
		{
			private final HttpClient  client = new DefaultHttpClient();
			private ProgressDialog Dialog = new ProgressDialog(ShowSearchBooksResultActivity.this);
			
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
					// Convert JSONObject to JSON to String
					String json = params[1];
					// Set json to StringEntity
					StringEntity se= new StringEntity(json);
					// Set httpPost Entity
					httpPost.setEntity(se);
					// Set some headers to inform server about the type of the content
					httpPost.setHeader("Content-Encoding", "UTF-8");
					httpPost.setHeader("Content-Type", "application/json");
					HttpResponse httpResponse = client.execute(httpPost);
					if(httpResponse.getStatusLine().getStatusCode() == 200 || httpResponse.getStatusLine().getStatusCode() == 201)
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
				Log.d("ShowSearchBooksResultActivity result", result);
				Dialog.dismiss();
				String status = null;
				String message = null;
				AlertDialog.Builder builder = new AlertDialog.Builder(ShowSearchBooksResultActivity.this);
				builder.setNeutralButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				try 
				{
					JSONObject jsonObj = new JSONObject(result);
					status = jsonObj.getString("Status");
				
					if(status.equals("True"))
					{
						builder.setTitle("Accepted");
						builder.setMessage("Succeed to reserve the book.\nIf the book is ready, a notice will be sent to you.");
					}
					else if(status.equals("False"))
					{
						message = jsonObj.getString("Message");
						builder.setTitle("Rejected");
						builder.setMessage(message);
					}
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
				builder.create().show();
			}
			
		}.execute(url, jsonObj.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(Generic.isLoggedIn())
		{
			getMenuInflater().inflate(R.menu.show_search_books_result, menu);
		}
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
				intent.setClass(ShowSearchBooksResultActivity.this, SearchBooksActivity.class);
		        startActivity(intent); 
		        return true;
	        
	    	case R.id.action_renew:
	        	
	            return true;
		}
	    return false;
    }
	
private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		// Invoke when Tick is click & ActionMode.finish()
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			listViewSearchResult.setItemChecked(-1, true);
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.show_search_books_result_on_listview_item_selected, menu);
	        return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
	            case R.id.action_renew:
	                HashMap<String,Object> bookItem = list.get(listViewSearchResult.getCheckedItemPosition());
	                String url = Generic.serverurl + "Reservation/PostReservation/";
	                
	                if(checkNetworkState())
	                {
	                	reserveBook(url, bookItem.get("Bid").toString());
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
