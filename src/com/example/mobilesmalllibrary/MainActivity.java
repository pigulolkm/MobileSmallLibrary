package com.example.mobilesmalllibrary;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViews();
	}
	
	private void findViews()
	{
		
	}
	
	////////////////////////
	// Login Button Click //
	////////////////////////
	public void login(View v)
	{
		
	}
	
	//////////////////////////////
	// Search Book Button Click //
	//////////////////////////////
	public void searchBook(View v)
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchBooksActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
