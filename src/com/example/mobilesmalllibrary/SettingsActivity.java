package com.example.mobilesmalllibrary;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}
	
	// Button SignIn Handler
	public void SignInGoogleAccount(View v)
	{
		// TODO Implement GCM client
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
