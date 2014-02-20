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
	private Button ButtonGenerateToken;
	private Button ButtonShowBorrowingRecord;
	
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
		ButtonGenerateToken = (Button)findViewById(R.id.ButtonGenerateToken);
		ButtonShowBorrowingRecord = (Button)findViewById(R.id.ButtonShowBorrowingRecord);
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
		if(Generic.loginToken == "0")
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
			Generic.loginToken = "0";
			
			setVisibilityGone();
		}
	}
	
	///////////////////////////////////
	// GOTO Search Book Button Click //
	///////////////////////////////////
	public void GOTOsearchBook(View v)
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchBooksActivity.class);
		startActivity(intent);
	}
	
	/////////////////////////////////
	// Generate Token Button Click //
	/////////////////////////////////
	public void GenerateToken(View v)
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, GenerateBorrowingTokenActivity.class);
		startActivity(intent);
	}
	
	private void setVisibilityGone()
	{
		ButtonGenerateToken.setVisibility(View.GONE);
		ButtonShowBorrowingRecord.setVisibility(View.GONE);
	}
	
	private void setVisibilityVisible()
	{
		ButtonGenerateToken.setVisibility(View.VISIBLE);
		ButtonShowBorrowingRecord.setVisibility(View.VISIBLE);
	}
	
	////////////////////////////////////////
	// Show Borrowing Record Button Click //
	////////////////////////////////////////
	public void ShowBorrowingRecord(View v)
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
					ButtonGoToLogin.setText(getString(R.string.Logout));
					setVisibilityVisible();
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
