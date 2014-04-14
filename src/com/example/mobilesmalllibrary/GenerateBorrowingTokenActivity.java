package com.example.mobilesmalllibrary;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class GenerateBorrowingTokenActivity extends Activity {
	
	private ImageView imageViewBorrowingToken;
	private Bitmap tokenImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generate_borrowing_token);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		findViews();
		GenerateToken();
	}
	
	private void findViews()
	{
		imageViewBorrowingToken = (ImageView)findViewById(R.id.imageViewBorrowingToken);
	}
	
	private void GenerateToken()
	{
		// Generate LoginToken QR code
		String QRPath = Generic.GoogleAPIGenerateQRUrl;
		QRPath = QRPath.replace("content", Generic.loginToken+"_"+Generic.LEmail+"_"+Generic.LID);
		
		if(checkNetworkState())
		{
			new GetTokenImageOperation().execute(QRPath);		
		}
	}
	
	private class GetTokenImageOperation extends AsyncTask<String, Void, Void>{
		
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(GenerateBorrowingTokenActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(true);
			Dialog.setTitle("Generating");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		
		protected Void doInBackground(String... params) {
			
			HttpGet httpGet = null;
			try
			{
				httpGet = new HttpGet(params[0]);
				HttpResponse httpResponse = client.execute(httpGet);
				if(httpResponse.getStatusLine().getStatusCode() == 200)
				{
					HttpEntity entity =  httpResponse.getEntity();
					
					if (entity != null) {
						InputStream inputStream = null;
					    try {
					        inputStream = entity.getContent();
					
					        tokenImage = BitmapFactory.decodeStream(inputStream);
					    } 
					    finally 
					    {
					        if (inputStream != null) 
					        {
					        	inputStream.close();
					        }
					        entity.consumeContent();
					    }
					}
				}
				else
				{
					throw new Exception();
				}
			}
			catch(Exception e)
			{
		         httpGet.abort();
		         AlertDialog.Builder dialog = new AlertDialog.Builder(GenerateBorrowingTokenActivity.this);
		         dialog.setTitle("Something went wrong while retrieving image. Please try again later.");
		         dialog.setNeutralButton("OK", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
		         });
		         dialog.create().show();
			}
			return null;
		}
		
		protected void onPostExecute(Void result)
		{
			Dialog.dismiss();
			if(tokenImage!=null)
	        {
				imageViewBorrowingToken.setImageBitmap(tokenImage);
	        }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.generate_borrowing_token, menu);
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
			intent.setClass(GenerateBorrowingTokenActivity.this,MainActivity.class);
	        startActivity(intent); 
	        return true;
		}
	    return false;
    }
	
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
