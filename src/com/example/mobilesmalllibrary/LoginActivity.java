package com.example.mobilesmalllibrary;

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
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private EditText EditTextEmail;
	private EditText EditTextPassword;
	
	private String Email;
	private String Password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		findViews();
	}
	
	private void findViews()
	{
		EditTextEmail = (EditText)findViewById(R.id.EditTextEmail);
		EditTextPassword = (EditText)findViewById(R.id.EditTextPassword);
	}
	
	////////////////////////
	// Login Button Click //
	////////////////////////
	public void login(View v)
	{
		//Reset errors.
		EditTextEmail.setError(null);
		EditTextPassword.setError(null);
		
		Email = EditTextEmail.getText().toString();
		Password = EditTextPassword.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(Password)) 
		{
			EditTextPassword.setError(getString(R.string.error_field_required));
			focusView = EditTextPassword;
			cancel = true;
		} 
		/*else if (Password.length() < 4) 
		{
			EditTextPassword.setError(getString(R.string.error_invalid_password));
			focusView = EditTextPassword;
			cancel = true;
		}*/

		// Check for a valid email address.
		if (TextUtils.isEmpty(Email)) 
		{
			EditTextEmail.setError(getString(R.string.error_field_required));
			focusView = EditTextEmail;
			cancel = true;
		} 
		else if (!Email.contains("@")) 
		{
			EditTextEmail.setError(getString(R.string.error_invalid_email));
			focusView = EditTextEmail;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			String url = Generic.serverurl + "LibraryUser/SignInLibraryUser";
			String passwordHash = Generic.computeHash(Password);
			JSONObject jsonObj = new JSONObject();
			
			try {
				jsonObj.put("pw", passwordHash);
				jsonObj.put("email", Email);
				
				String[] params = new String[]{ url, jsonObj.toString() };
				
				if(checkNetworkState())
				{
					new loginOperation().execute(params);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class loginOperation extends AsyncTask<String, Void, String>{
		
		private final HttpClient  client = new DefaultHttpClient();
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);
		
		@Override
		protected void onPreExecute() {
			Dialog.setCancelable(true);
			Dialog.setTitle("Loading");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			String result = null;
			try
			{
				HttpPut httpPut = new HttpPut(params[0]);
				// Convert JSONObject to JSON to String
				String json = params[1];
				// Set json to StringEntity
				StringEntity se= new StringEntity(json);
				// Set httpPost Entity
				httpPut.setEntity(se);
				// Set some headers to inform server about the type of the content
				httpPut.setHeader("Content-Encoding", "UTF-8");
				httpPut.setHeader("Content-Type", "application/json");
				HttpResponse httpResponse = client.execute(httpPut);
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
			try {
				JSONArray jsonArray = new JSONArray(result);
				
				if(jsonArray.length() != 0)
				{			
					JSONObject jsonObj = jsonArray.getJSONObject(0);
					// True means account exist and password correct. Token is generated.
					if(jsonObj.getString("result").equals("True"))
					{
						Generic.loginToken = jsonObj.getString("token");
						Generic.LID = jsonObj.getString("LID");
						Generic.LEmail = Email;
						
						Intent returnIntent = new Intent();
						returnIntent.putExtra("name",jsonObj.getString("name"));
						returnIntent.putExtra("lastLoginTime", jsonObj.getString("lastLoginTime"));
						setResult(RESULT_OK,returnIntent);     
						finish();
					}
					else
					{
						Toast.makeText(LoginActivity.this, "Invalid email/ password", Toast.LENGTH_LONG).show();
					}
				}
			}catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
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
				}
        	}).create().show();;
        }
		return false;
	}
}
