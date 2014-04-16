package com.example.mobilesmalllibrary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowAnnouncementActivity extends Activity {

	private TextView textViewAnnouncementContent;
	private TextView textViewAnnouncementTime;
	private int posOfSelectedItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_announcement);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		posOfSelectedItem =  intent.getIntExtra("Pos", 0);
		
		findViews();
		loadAnnouncement(Generic.announcementJson);
	}
	
	private void findViews()
	{
		textViewAnnouncementContent = (TextView)findViewById(R.id.textViewAnnouncementContent);
		textViewAnnouncementTime = (TextView)findViewById(R.id.textViewAnnouncementTime);
	}
	
	private void loadAnnouncement(String json)
	{
		try {
			JSONArray jsonArray = new JSONArray(json);
			
			if(jsonArray.length() != 0)
			{			
				JSONObject jsonObj = jsonArray.getJSONObject(posOfSelectedItem);
				String[] msg = jsonObj.getString("A_content").split(":");
				String datetime = jsonObj.getString("A_datetime").replace("T", " ");
				String[] temp = datetime.split(":");
				String YYYYmmddHHMM = temp[0] + ":" + temp[1];
				
				textViewAnnouncementContent.setText(msg[1]);
				textViewAnnouncementTime.setText(YYYYmmddHHMM);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.show_announcement, menu);
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
			intent.setClass(ShowAnnouncementActivity.this, MainActivity.class);
	        startActivity(intent); 
	        return true;
		}
	    return false;
    }

}
