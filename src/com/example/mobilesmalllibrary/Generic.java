package com.example.mobilesmalllibrary;

import java.security.MessageDigest;


public class Generic {
	public static final int scan_REQUEST = 1;
	public static final int signIn = 2;
	
	public static String serverurl = "http://piguloming.no-ip.org:90/api/";
	public static String GoogleAPIGenerateQRUrl = "http://chart.apis.google.com/chart?cht=qr&chl=content&chs=200x200";
	public static String loginToken = "0";
	public static String LID = "";
	
	public static String computeHash(String input) {
		StringBuffer sb = new StringBuffer();
		try
		{
		    MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    digest.reset();
	
		    byte[] byteData = digest.digest(input.getBytes("UTF-8"));

		    for (int i = 0; i < byteData.length; i++){
		      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		    }
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	    return sb.toString();
	}
}
