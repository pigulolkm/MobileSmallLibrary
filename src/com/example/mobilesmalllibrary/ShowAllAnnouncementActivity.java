package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ShowAllAnnouncementActivity extends Activity {

	private String allAnnouncementJson;
	private ListView ListViewAllAnnouncementResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_all_announcement);
		
		Intent intent = getIntent();
		allAnnouncementJson = intent.getStringExtra("AllAnnouncementJson");
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		findViews();
		showAllAnnouncement(allAnnouncementJson);
	}
	
	private void findViews()
	{
		ListViewAllAnnouncementResult = (ListView)findViewById(R.id.ListViewAllAnnouncementResult);
		ListViewAllAnnouncementResult.setOnItemClickListener(new announcementOnItemClick());
	}
	
	private void showAllAnnouncement(String json)
	{
		ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> item;
		
		try {
			JSONArray jsonArray = new JSONArray(json);

			if(jsonArray.length() != 0)
			{			
				JSONObject jsonObj;
				
				for(int i = 0; i < jsonArray.length(); i++)
				{
					jsonObj = jsonArray.getJSONObject(i);
					item = new HashMap<String,Object>();
					
					String[] msg = jsonObj.getString("A_content").split(":");
					String datetime = jsonObj.getString("A_datetime").replace("T", " ");
					String[] temp = datetime.split(":");
					String YYYYmmddHHMM = temp[0] + ":" + temp[1];
					
					item.put("msg", msg[1]);
					item.put("datetime", YYYYmmddHHMM);
					list.add(item);
				}
				
				SimpleAdapter adapter = new SimpleAdapter(ShowAllAnnouncementActivity.this, list, R.layout.listview_announcement_item, 
						new String[]{"msg","datetime"},
						new int[]{R.id.textViewAnnouncementContent, R.id.textViewAnnouncementDatetime});
				
				ListViewAllAnnouncementResult.setAdapter(adapter);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class announcementOnItemClick implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long arg3) 
		{
			Intent intent = new Intent();
			intent.setClass(ShowAllAnnouncementActivity.this, ShowAnnouncementActivity.class);
			intent.putExtra("Pos", pos);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.show_all_announcement, menu);
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
			intent.setClass(ShowAllAnnouncementActivity.this,MainActivity.class);
	        startActivity(intent); 
	        return true;
		}
	    return false;
    }
}
