package com.shakein;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Activity context;
	private Button rePassButton;
	private Button reWordButton;
	private Switch openLockingSwitch;
	private boolean isInCreate;
	private Button button1;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        createFile();
        boolean isFirstRun=isFirstRun();
        if(isFirstRun==true){
        	Intent intent=new Intent(this,welcome.class);
        	this.startActivity(intent);
        	finish();
        }else{
        	SharedPreferences pref=context.getSharedPreferences("ShakeIn",Context.MODE_PRIVATE);
			String password= pref.getString("word", "");
			if(password==null || password.equals("")){
				Intent intent = new Intent(context, setKeyActivity.class);
				context.startActivity(intent);			
				context.finish();
			}else{
				loadSettingView();
	        	setLockingStatus();
			}
        }
      

    }
    private boolean isFirstRun() {
		SharedPreferences ShakeInPref = getSharedPreferences(
				"ShakeIn", 0);
		boolean b = ShakeInPref.getBoolean("isFirstRun", true);
		Editor editor=ShakeInPref.edit();
		if (b == true) {
			editor.putBoolean("isFirstRun", false);
			editor.commit();
			return true;
		}
		return false;
	}
    private void loadSettingView(){
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	context.setContentView(R.layout.main_activity);
    	button1=(Button)findViewById(R.id.button_back_main);
    	//context.setContentView(R.layout.key3);
    	rePassButton=(Button)findViewById(R.id.button1);
    	reWordButton=(Button)findViewById(R.id.button2);
    	openLockingSwitch=(Switch)findViewById(R.id.switch_start);
    	
    	button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
    	
    	reWordButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(context,reSetKeyActivity.class);
				context.startActivity(intent);
			}
		});
    	openLockingSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
				// TODO Auto-generated method stub
				boolean isSuccess = registerLockingStatus(checked);
				if(isSuccess){
					SharedPreferences ShakeInPref = getSharedPreferences(
							"ShakeIn", 0);
					Editor editor = ShakeInPref.edit();
					editor.putBoolean("isLockingOpened", checked);
					editor.commit();
				}else {
					openLockingSwitch.setChecked(!checked);
				}
			}
			public boolean registerLockingStatus(boolean isSet) {
				Intent intent = new Intent(context, lockService.class);
				if (isSet) {
					//lockService.flag1=1;
					context.startService(intent);
				} else {
					//lockService.flag1=0;
					// Shut off the screen locking app;
					context.stopService(intent);
				}
				return true;
			}
		});
    	
    	rePassButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String sd_card = Environment.getExternalStorageDirectory().toString();
			    String path = sd_card + "/shakeIn/data0.txt";
			    File file=new File(path);
			    if(file.exists()) file.delete();
				Intent intent =new Intent(context,sampleShakingActivity.class);
				//intent.putExtra("isRePass", "true");
				context.startActivity(intent);
			}
		});
    }
    public void createFile() {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
			File sd=Environment.getExternalStorageDirectory();
			String path=sd.getPath()+"/shakeIn";
			File file=new File(path);
			if(!file.exists()){
				file.mkdir();
				//writeData();
			}
		}
			
	}
    private boolean isLockingOpened() {
		SharedPreferences ShakeInPref = getSharedPreferences(
				"ShakeIn", 0);
		boolean b = ShakeInPref.getBoolean("isLockingOpened", false);
		if (b == true) {
			return true;
		} else {
			return false;
		}
	}
    private void setLockingStatus() {
		boolean isLockingOpened = isLockingOpened();
		openLockingSwitch.setChecked(isLockingOpened);
	}
   


}
