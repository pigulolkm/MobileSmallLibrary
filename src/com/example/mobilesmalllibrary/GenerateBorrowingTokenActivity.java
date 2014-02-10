package com.example.mobilesmalllibrary;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class GenerateBorrowingTokenActivity extends Activity {
	
	private ImageView imageViewBorrowingToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generate_borrowing_token);
		
		findViews();
	}
	
	private void findViews()
	{
		imageViewBorrowingToken = (ImageView)findViewById(R.id.imageViewBorrowingToken);
	}
	
	///////////////////////////
	// Generate Button Click //
	///////////////////////////
	private void GenerateToken(View v)
	{
		// Generate LoginToken QR code
		String QRPath = Generic.GoogleAPIGenerateQRUrl;
		QRPath = QRPath.replace("content", Generic.loginToken);
		
		// TODO Follow the book marks of "Download image" to implement the following part
		// 1. Get the QR image
		// 2. set image source to imageViewBorrowingToken.setImageBitmap(bm);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.generate_borrowing_token, menu);
		return true;
	}

}
