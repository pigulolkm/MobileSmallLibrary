package com.example.mobilesmalllibrary;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView TextViewWelcome;
	private TextView TextViewLastLoginTime;
	private ListView ListViewAnnouncementResult;
	
	private Menu menu;
	private DrawerLayout layDrawer;
    private ListView lstDrawer;
    private String[] drawer_menu;

    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle = "Home";	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		checkNetworkState();
		initActionBar();
		initDrawer();
		// To set up the list of options on the left slider menu
        initDrawerList();
        getActionBar().setTitle(getTitle());
		
		findViews();
		if(Generic.announcementJson == "")
		{
			if(Generic.isOnline(this))
			{
				new downloadLastFiveAnnouncement().execute();
			}
		}
		else
		{
			showAnnouncement(Generic.announcementJson);
		}
	}
	
	private void findViews()
	{
		TextViewWelcome = (TextView)findViewById(R.id.TextViewWelcome);
		TextViewLastLoginTime = (TextView)findViewById(R.id.TextViewLastLoginTime);
		ListViewAnnouncementResult = (ListView)findViewById(R.id.ListViewAnnouncementResult);
		ListViewAnnouncementResult.setOnItemClickListener(new announcementOnItemClick());
	}
	
	private class downloadLastFiveAnnouncement extends AsyncTask<Void, Void, String>
	{
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(true);
			Dialog.setCanceledOnTouchOutside(false);
			Dialog.setTitle("Loading");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		
		@Override
		protected String doInBackground(Void...params) {
			
			String result = null;
			try
			{
				HttpGet httpGet = new HttpGet(Generic.serverurl + "Announcement/GetLastFiveAnnouncements");
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
			Generic.announcementJson = result;
			showAnnouncement(Generic.announcementJson);
			Log.d("MainActivity", result);
		}
	}
	
	private void showAnnouncement(String json)
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
				
				SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, list, R.layout.listview_announcement_item, 
						new String[]{"msg","datetime"},
						new int[]{R.id.textViewAnnouncementContent, R.id.textViewAnnouncementDatetime});
				
				ListViewAnnouncementResult.setAdapter(adapter);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	private void checkNetworkState()
	{
        if(!Generic.isOnline(this))
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
	}
	
	private void initActionBar(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }
	
	private void initDrawer(){
        setContentView(R.layout.activity_main);

        layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lstDrawer = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        layDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        lstDrawer.setOnItemClickListener(new DrawerItemClickListener());

        drawerToggle = new ActionBarDrawerToggle(
                this, 
                layDrawer,
                R.drawable.ic_drawer, 
                R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(getTitle());
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };
        drawerToggle.syncState();

        layDrawer.setDrawerListener(drawerToggle);
    }
	
	private void initDrawerList(){
		ArrayAdapter<String> adapter;
		if(Generic.loginToken == "0")
		{
			drawer_menu = this.getResources().getStringArray(R.array.drawer_menu_not_login);
			adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer_menu);
		}
		else
		{
			drawer_menu = this.getResources().getStringArray(R.array.drawer_menu_login);
			adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer_menu);
		}
        lstDrawer.setAdapter(adapter);
    }
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}
	
	private void selectItem(int position) {
		
		if(Generic.loginToken != "0") // logged in
		{
		    switch (position) {
			    case 0: // Home
			    	
			    	break;
			    case 1: // Logout
			        Logout();
			    	break;
			    case 2: // Search Books
			    	GOTOsearchBook();
			    	break;
			    case 3: // Generate Token
			    	GenerateToken();
			    	break;
			    case 4: // Borrowing Record
			    	ShowBorrowingRecord();
			    	break;
			    default:
			    	
			        return;
		    }
		}
		else // not login
		{
			switch (position) {
				case 0: // Home
					
					break;
				case 1: // Login
			        Login();
			    	break;
			    case 2: // Search Books
			    	GOTOsearchBook();
			    	break;
			    default:
			    	
			        return;
		    }
		}

	    lstDrawer.setItemChecked(position, true);
	    // setTitle(drawer_menu[position]);
	    layDrawer.closeDrawer(lstDrawer);
	}
	
	public void Login()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, LoginActivity.class);
		startActivityForResult(intent, Generic.signIn);
	}
	
	public void Logout()
	{
		TextViewWelcome.setText("");
		TextViewLastLoginTime.setText("");
		Generic.resetAccountInfo();
		
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setClass(MainActivity.this,MainActivity.class);
        startActivity(intent);
        
        onCreateOptionsMenu(this.menu);
	}
	
	public void GOTOsearchBook()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchBooksActivity.class);
		startActivity(intent);
	}
	
	public void GenerateToken()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, GenerateBorrowingTokenActivity.class);
		startActivity(intent);
	}
	
	public void ShowBorrowingRecord()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, ShowBorrowingRecordActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case Generic.signIn:
				if(resultCode == RESULT_OK)
				{
					TextViewWelcome.setText("Welcome, "+data.getStringExtra("name")+"!");
					TextViewLastLoginTime.setText("Last login time : "+data.getStringExtra("lastLoginTime"));
					initDrawerList();
                } 
                break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.menu = menu;
		if(Generic.loginToken != "0")
		{
			getMenuInflater().inflate(R.menu.main, menu);
		}
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    
	    switch(item.getItemId())
	    {
	    	// setting
	    	case R.id.action_settings:
	    		Intent intent = new Intent();
	    		intent.setClass(MainActivity.this, SettingsActivity.class);
	    		startActivity(intent);
	    		return true;
	    		
	    	case android.R.id.home:
			    //Home icon is selected
			    if (drawerToggle.onOptionsItemSelected(item)) 
			    {
			        return true;
			    }
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public class announcementOnItemClick implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long arg3) 
		{
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, ShowAnnouncementActivity.class);
			intent.putExtra("Pos", pos);
			startActivity(intent);
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(this.menu != null)
		{
			onCreateOptionsMenu(this.menu);
		}
		
		if(Generic.announcementJson == "")
		{
			if(Generic.isOnline(this))
			{
				new downloadLastFiveAnnouncement().execute();
			}
		}
		else
		{
			showAnnouncement(Generic.announcementJson);
		}
	}
	
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Are you sure you want to exit?");
		
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Generic.resetAccountInfo();
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}
