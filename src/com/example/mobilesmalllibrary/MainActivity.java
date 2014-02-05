package com.example.mobilesmalllibrary;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView TextViewWelcome;
	private Button ButtonGoToLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViews();
		checkNetworkState();
	}
	
	private void findViews()
	{
		TextViewWelcome = (TextView)findViewById(R.id.TextViewWelcome);
		ButtonGoToLogin = (Button)findViewById(R.id.ButtonGoToLogin);
	}
	
	private void checkNetworkState()
	{
		// TODO Check network
	}
	
	////////////////////////
	// GOTO Login Button Click //
	////////////////////////
	public void GOTOloginOut(View v)
	{
		// Login
		if(Generic.loginToken.equals(null))
		{
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent, Generic.signIn);
		}
		// Logout
		else
		{
			ButtonGoToLogin.setText(getString(R.string.Login));
			TextViewWelcome.setText("");
			Generic.loginToken = null;
		}
	}
	
	//////////////////////////////
	// GOTO Search Book Button Click //
	//////////////////////////////
	public void GOTOsearchBook(View v)
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchBooksActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case Generic.signIn:
				if(resultCode == RESULT_OK)
				{
					TextViewWelcome.setText("Welcome, "+data.getStringExtra("name")+"!");
					ButtonGoToLogin.setText(getString(R.string.Logout));
                } 
                break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
