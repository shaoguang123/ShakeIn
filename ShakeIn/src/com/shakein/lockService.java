package com.shakein;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import libsvm.svm;
import libsvm.svm_model;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class lockService extends Service {

	public static final String LOCK_ACTION = "lock";
	public static final String UNLOCK_ACTION = "unlock";
	public static final String RemoveView2 = "remove";
	private Context mContext;
	private WindowManager mWinMng;
	private lockView screenView1;
	private lockDialogView screenView2;
	
	private SensorManager sensorManager=null;
	private Context context;
	private float[] accValues;
	private float[] gyrValues;
	private ArrayList<Tuple[]> sensorData= new ArrayList<Tuple[]>();
	private int flag2=0;
	private Tuple[] values=new Tuple[2];
	public double f;
	private int cnt=0;
	private double timeStarted;
	private double timeEnded;
	int n=0;
	public static svm_model model=null;
	int times=0;
	
	
	private KeyguardManager mKeyguardManager=null;
	private KeyguardManager.KeyguardLock mKeyguardLock=null;
	
	private Intent i;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate(){

		super.onCreate();
		mContext=getApplicationContext();
		mWinMng=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		
		i=new Intent(mContext,lockService.class);
		i.setAction(lockService.LOCK_ACTION);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context = this;
		
		mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock("myLock1");
		mKeyguardLock.disableKeyguard();
		
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		lockService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		
		/*注册广播*/
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		lockService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);
		
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		SharedPreferences ShakeInPref = getSharedPreferences(
				"ShakeIn", 0);
		boolean b = ShakeInPref.getBoolean("isLockingOpened", false);
		if (b==false) {
			Intent i = new Intent(context, lockService.class);
			stopService(i);
		}
		else if (intent!=null) {
			String action=intent.getAction();
			if (TextUtils.equals(action, LOCK_ACTION)) {
				mKeyguardManager = (KeyguardManager) mContext.getSystemService(mContext.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("myLock1");
				mKeyguardLock.disableKeyguard();
				addView1();
			} else if(TextUtils.equals(action, UNLOCK_ACTION)){
				if(sensorManager != null){
					sensorManager.unregisterListener(myListener);
				}
				removeView1();
				
			}else if (TextUtils.equals(action, RemoveView2)) {
				removeView2();
				times=0;
			}
		}
		return Service.START_STICKY;
	}
	
	public void addView1(){
		if(screenView1 == null){
			screenView1 = new lockView(mContext);
			
			LayoutParams param=new LayoutParams();
			param.type=LayoutParams.TYPE_SYSTEM_ALERT;
			//param.type=2005;
			param.format=PixelFormat.RGBA_8888;
			param.width = LayoutParams.MATCH_PARENT;
			param.height=LayoutParams.MATCH_PARENT;
			
			mWinMng.addView(screenView1, param);
			
		}
	}
	
	public void removeView1(){
		if(screenView1!=null){
			mWinMng.removeView(screenView1);
			screenView1=null;
		}
	}
	
	public void addView2(){
		if(screenView2 == null){
			screenView2 = new lockDialogView(mContext);
			
			LayoutParams param=new LayoutParams();
			param.type=LayoutParams.TYPE_SYSTEM_ALERT;
			//param.type=2005;
			param.format=PixelFormat.RGBA_8888;
			param.width = LayoutParams.MATCH_PARENT;
			param.height=LayoutParams.MATCH_PARENT;
			
			mWinMng.addView(screenView2, param);
			
		}
	}
	
	public void removeView2(){
		if(screenView2!=null){
			mWinMng.removeView(screenView2);
			screenView2=null;
		}
	}
	
	
	private BroadcastReceiver mScreenOnReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if (action.equals("android.intent.action.SCREEN_ON")) {
				mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("myLock1");
				mKeyguardLock.disableKeyguard();
				
				flag2=0;
				n=0;
				sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
				sensorManager.registerListener(myListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),5000);
				sensorManager.registerListener(myListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),5000);
				timeStarted=System.currentTimeMillis();
				cnt=0;
				values[0]=null;values[1]=null;
				sensorData.clear();
				
				SharedPreferences ShakeInPref = getSharedPreferences(
						"ShakeIn", 0);
				boolean b = ShakeInPref.getBoolean("isLockingOpened", false);
				if (b) {
					startService(i);
				}else {
					Intent i = new Intent(context, lockService.class);
					stopService(i);
				}

			}
			// TODO Auto-generated method stub
			
		}
	};
	
private BroadcastReceiver mScreenOffReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("myLock1");
				mKeyguardLock.disableKeyguard();
				
				//startService(new Intent(lockService.this, lockService.class));
				SharedPreferences ShakeInPref = getSharedPreferences(
						"ShakeIn", 0);
				boolean b = ShakeInPref.getBoolean("isLockingOpened", false);
				if (b) {
					startService(i);
				}else {
					Intent i = new Intent(context, lockService.class);
					stopService(i);
				}

			}
			// TODO Auto-generated method stub
			
		}
	};
	
	public void onDestroy(){
		super.onDestroy();
		lockService.this.unregisterReceiver(mScreenOnReceiver);
		lockService.this.unregisterReceiver(mScreenOffReceiver);
		SharedPreferences ShakeInPref = getSharedPreferences(
				"ShakeIn", 0);
		boolean b = ShakeInPref.getBoolean("isLockingOpened", false);
		if (b) {
			Intent i = new Intent(context, lockService.class);
			startService(i);
		}else {
			Intent i = new Intent(context, lockService.class);
			stopService(i);
		}
		if(sensorManager != null){
			sensorManager.unregisterListener(myListener);
		}
		
	}
	
private SensorEventListener myListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {

			switch (event.sensor.getType()) {
			case Sensor.TYPE_LINEAR_ACCELERATION:
				accValues=event.values.clone();
				float a_x=accValues[0];
				float a_y=accValues[1];
				float a_z=accValues[2];
				values[0]=new Tuple(a_x, a_y, a_z);
				break;
			case Sensor.TYPE_GYROSCOPE:
				gyrValues=event.values.clone();
				float w_x=gyrValues[0];
				float w_y=gyrValues[1];
				float w_z=gyrValues[2];
				values[1]=new Tuple(w_x, w_y, w_z);	
				cnt++;
				break;
			default:
				break;
			}
			if (flag2==0 && values[0]!=null && values[1]!=null && Tuple.mod(new Tuple(values[0]))>Analysis.shakeJudgeStart) {
				flag2=1;
			}
			if(flag2==1 && values[0]!=null && values[1]!=null){
				if (Tuple.mod(new Tuple(values[0]))<Analysis.ShakeJudgeEnd) {
					flag2=2;
					n++;
					if(n<50){
						Toast.makeText(context, "连续摇晃时间过短,请重新摇晃", Toast.LENGTH_SHORT).show();
						values[0]=null;
						values[1]=null;
						sensorData.clear();
						flag2=0;
						n=0;
					}
					else {
						sensorData.add(new Tuple[]{new Tuple(values[0]),new Tuple(values[1])});
						timeEnded=System.currentTimeMillis();
						f=(cnt/(timeEnded-timeStarted))*1000;
						match.init(context,msgHandler,f);
						match.receiveData(sensorData);
					}
					
					
				}else{
					n++;
					sensorData.add(new Tuple[]{new Tuple(values[0]),new Tuple(values[1])});
					values[0]=null;
					values[1]=null;
				}
				
			}
			
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	};
	private Handler msgHandler= new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==2){
				Intent i = new Intent(mContext,lockService.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				i.setAction(lockService.UNLOCK_ACTION);
				mContext.startService(i);
				times=0;
			}else if (msg.what==3) {
				times++;
				VibratorUtil.Vibrate(mContext, 200);
				if(times>=3) {
					addView2();
					times=0;
				}
				flag2=0;
				values[0]=null;values[1]=null;
				sensorData.clear();
				n=0;
			}
			
		}
	};
	
	

}
