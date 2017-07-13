package com.fvd.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.preference.PreferenceManager;


public class appSettings {
	private static appSettings singleton;
	public static final String SaveDir= "Nimbus Clipper";
	public static String SavingPath;
	public static String userMail = "";
	public static String userPass ="";
	public static String sessionId = "";
	public static String service = "";
	public static String pocket_code = "";
	public static String defFolderId = "";
	public static String img_path = "";
	public static int photo_quality=90;
	private static boolean isTablet=false;
	public static appSettings getInstance(){
		if (singleton == null)
        {
			singleton = new appSettings();
        }
        return singleton;
	}

	private appSettings() {
		
		SavingPath=Environment.getExternalStorageDirectory() + "/"+SaveDir+"/";
		CheckDirectory(SavingPath);
	}
	
	public static void storeUserData(Context c, String user, String pass, String sess, String srv){
		userMail = user;
		userPass = pass;
		sessionId=sess;
		service=srv;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = prefs.edit();
		e.putString("userMail", user);
		e.putString("userPass", pass);
		e.putString("sessionId", sess);
		e.putString("service", srv);
		e.commit();
	}
	
	public static void prefsWriteString(Context c, String k, String v) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = prefs.edit();
		e.putString(k, v);
		e.commit();
	}
	
	public static void storePocket(Context c,String v) {
		pocket_code=v;
		prefsWriteString(c, "pocket_code", v);
	}
	
	public static void resetPocket(Context c) {
		pocket_code="";
		prefsWriteString(c, "pocket_code", "");
	}
	
	public static void SignOut(Context c){
		storeUserData(c, "", "", "","");
	}
	
	public static String prefsReadString(Context c, String key, String def){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getString(key, def);
		
	}
	
	public static boolean getBoolean(Context c, String key, boolean def){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getBoolean(key, def);
		
	}
	
	static boolean inited=false;
	
	public static void init(Context c) {
		initCache(c);
		if(!inited){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
			userMail = prefs.getString("userMail", "");
	        userPass = prefs.getString("userPass", "");
	        sessionId = prefs.getString("sessionId", "");
	        service = prefs.getString("service", "");
	        pocket_code = prefs.getString("pocket_code", "");
	        img_path=prefs.getString("imgFolderPath", Environment.getExternalStorageDirectory().getPath() + "/"+SaveDir+"/");
	        photo_quality=Integer.parseInt(prefs.getString("photoQuality", "90"));
	        inited=true;
		}
		
	}
	
	private void CheckDirectory(String path){
    	File dir=new File(path);
    	if(!dir.exists()){
    	 dir.mkdirs();
    	}
    	else{
    		File[] files=dir.listFiles();
       	 	for (int i=0; i<files.length; i++){
       	 		if(files[i].isFile() && files[i].getName().contains("-tmp.png")){
       	 			files[i].delete();
       	 		}
       	 	}
    	}
    }
	
	public static boolean isFileExists(String file){
		File f=new File(file);
		return f.exists();
	}
	

	
	public static String saveTempBitmap(Bitmap bm, boolean isPhoto){
		String savingPath=getSavingPath();
		String	photoFileName=String.valueOf(System.currentTimeMillis())+"-tmp.jpg";
		 	File file = new File(savingPath,photoFileName); 
		 	try{
		 		file.createNewFile();
		 		FileOutputStream ostream = new FileOutputStream(file);
		 		bm.compress(CompressFormat.JPEG, 95, ostream);
		 		ostream.flush();
		 		ostream.close();
		 	}
		 	catch (Exception e){
		 		photoFileName ="";
		 		appendLog("saveTempBitmap: "+e.getMessage());
		 	}
		 	return String.format("%s%s", savingPath , photoFileName);
	}
	public static String cacheDir="";
	public static String getSavingPath(){
		return  cacheDir + "/";//Environment.getExternalStorageDirectory().getPath() + "/"+SaveDir+"/"; 
	}
	
	public static void initCache(Context ctx){
		if(appSettings.cacheDir==""){
			File ex=ctx.getExternalCacheDir();
	        if (ex==null) ex=ctx.getCacheDir();
	        if (ex==null) ex=Environment.getExternalStorageDirectory();
	        if(ex!=null) appSettings.cacheDir=ex.getPath();
		}
	}
	
	public static void log(String text) {
		File logFile = new File(Environment.getExternalStorageDirectory().getPath()+"/"+"nimbus_log.txt");
		   if (!logFile.exists())
		   {
		      try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		   }
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(text);
		      buf.newLine();
		      buf.close();
		   }
		   catch (IOException e)
		   {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		   }
	}
	
	public static void appendLog(String text)
	{       
	   /*File logFile = new File(Environment.getExternalStorageDirectory().getPath()+"/"+"nimbus.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }*/
	}
	
	public static void saveToSD(String text, String fName)
	{       
	   File logFile = new File(Environment.getExternalStorageDirectory()+"/"+ fName);
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	public void setIsTablet(boolean b){
		isTablet = b;
	}
	
	public static boolean getIsTablet(){
		return isTablet;
	}
}
