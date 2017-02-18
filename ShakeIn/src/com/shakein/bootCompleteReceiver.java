package com.shakein;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

public class bootCompleteReceiver extends BroadcastReceiver {

	String myPkgName = "com.shakein";//#包名
	String myActName = "com.shakein.lockService";//#类名
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "hello", Toast.LENGTH_LONG).show();
		Intent myIntent=new Intent();
		myIntent.setAction(lockService.LOCK_ACTION);
		myIntent.setClassName(myPkgName, myActName);
		context.startService(myIntent);
		// TODO Auto-generated method stub


	}

}