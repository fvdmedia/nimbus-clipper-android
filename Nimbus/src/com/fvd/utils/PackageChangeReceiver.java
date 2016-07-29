package com.fvd.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PackageChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
    	Uri data = intent.getData();
    	/*	Log.d(TAG, "Action: " + intent.getAction());
			Log.d(TAG, "The DATA: " + data);
    	 * boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
    	 * 	ACTION_PACKAGE_REMOVED 
			ACTION_PACKAGE_ADDED 
			ACTION_PACKAGE_REPLACED 
    	 * 
    	 */
    }
   
}
