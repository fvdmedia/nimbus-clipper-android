package com.fvd.utils;

import com.fvd.nimbus.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class AppRate {
	private final static String APP_TITLE = "Nimbus Clipper";     
    private static String APP_PACKAGE_NAME = "com.fvd.nimbus";
    
    private final static int DAYS_UNTIL_PROMPT = 1;
    private final static int LAUNCH_UNTIL_PROMPT = 3;
 
    public static void start(Context mContext){
    	APP_PACKAGE_NAME = mContext.getApplicationInfo().packageName;
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (prefs.getBoolean("dontshowagain", false)){return;}
        SharedPreferences.Editor editor = prefs.edit();

        long launch_count = prefs.getLong("launch_count", 0) +1;
        editor.putLong("launch_count", launch_count);
        
        Long date_firstLaunch = prefs.getLong("ms",0);
        
        //Wait at least X days to launch
        if (launch_count >= LAUNCH_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)){
                showRateDialog(mContext, editor);
            }
        }
        
        editor.commit();
    }
    
    public static void sendMail(Context ctx, String subject){
    	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "nimbus@everhelper.me" });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
            "Write your problem with as much detail as possible below");

        ctx.startActivity(Intent.createChooser(emailIntent,"Nimbus feedback"));
    }
 
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor){
    	Dialog dialog = new Dialog(mContext);
    	 
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String message = mContext.getString(R.string.rate_text);
        builder.setMessage(message)
                .setTitle(mContext.getString(R.string.rate) +" " + APP_TITLE)
                .setIcon(mContext.getApplicationInfo().icon)
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.rate_awesome),
                        new DialogInterface.OnClickListener() {
 
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            	if (editor != null) {
                                    editor.putBoolean("dontshowagain", true);
                                    editor.commit();
                                }
                                try{
                                	mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
                                }
                                catch(Exception e){
                                }
                                dialog.dismiss();
                            }
                        })
                .setNeutralButton(mContext.getString(R.string.rate_average),        
                        new DialogInterface.OnClickListener() {
 
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            	if (editor != null) {
                                    editor.putBoolean("dontshowagain", true);
                                    editor.commit();
                                }
                                dialog.dismiss();
                                try{
                                	sendMail(mContext,"Suggestion");
                                }
                                catch(Exception e){
                                	
                                }
 
                            }
                        })
                .setNegativeButton(mContext.getString(R.string.rate_sucks),
                        new DialogInterface.OnClickListener() {
 
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (editor != null) {
                                    editor.putBoolean("dontshowagain", true);
                                    editor.commit();
                                }
                                dialog.dismiss();
                                try{
                                	sendMail(mContext,"Bug Report");
                                }
                                	catch(Exception e){
                                }
 
                            }
                        });
        dialog = builder.create();
 
        dialog.show();
    }
}
