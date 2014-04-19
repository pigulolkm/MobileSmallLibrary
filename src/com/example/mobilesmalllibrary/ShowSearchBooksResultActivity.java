package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ShowSearchBooksResultActivity extends Activity {

	private ListView listViewSearchResult;
	
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
	}
	
	public void showBookSearchedResult(String result)
	{
		ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> item;
		
		try {
			JSONArray jsonArray = new JSONArray(result);

			if(jsonArray.length() != 0)
			{			
				JSONObject jsonObj;
				
				for(int i = 0; i < jsonArray.length(); i++)
				{
					jsonObj = jsonArray.getJSONObject(i);
					item = new HashMap<String,Object>();
					
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.show_search_books_result, menu);
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
		}
	    return false;
    }

}
