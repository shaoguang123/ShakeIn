package com.shakein;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

public class networkChangeReceiver extends BroadcastReceiver {
	
	String myPkgName = "com.shakein";//#包名
	String myActName = "com.shakein.lockService";//#类名
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "network changed", Toast.LENGTH_LONG).show();
		Intent myIntent=new Intent();
		//myIntent.setAction(lockService.LOCK_ACTION);
		myIntent.setClassName(myPkgName, myActName);
		context.startService(myIntent);
		// TODO Auto-generated method stub
		SharedPreferences pref=context.getSharedPreferences("ShakeIn",Context.MODE_PRIVATE);
		int tim= pref.getInt("times", 0);
		if(tim%5==1 || tim%5==0){
			String sd_card = Environment.getExternalStorageDirectory().toString();
		    String path = sd_card + "/shakeIn";
		    String train_path = path + "/data0.txt";
		    String model_name = path + "/my_model.txt";
		    String[] trainArgs = {"-s","2","-n","0.2",train_path, model_name};
		    svm_train train = new svm_train();
		    try {
				svm_train.main(trainArgs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		

	}

}
