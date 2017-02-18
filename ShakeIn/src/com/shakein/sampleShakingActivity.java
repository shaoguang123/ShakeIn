package com.shakein;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class sampleShakingActivity extends FragmentActivity{
	private ViewPager viewPager;
	
	private SensorManager sensorManager=null;
	private Context context;
	private float[] accValues;
	private float[] gyrValues;
	private ArrayList<Tuple[]> sensorData= new ArrayList<Tuple[]>();
	private int flag=0;
	private Tuple[] values=new Tuple[2];
	public double f;
	private int cnt=0;
	private double timeStarted;
	private double timeEnded;
	int n=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shake_sample);
        ActivityCollector.addActivity(this);
        SharedPreferences pref=getSharedPreferences("ShakeIn",Context.MODE_PRIVATE);
        Editor editor=pref.edit();
		editor.putInt("times", 0);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new Adapter(getSupportFragmentManager()));
        context = this;
        flag=0;
		sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		//sensorManager.registerListener(myListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),sensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(myListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),5000);
		sensorManager.registerListener(myListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),5000);
		timeStarted=System.currentTimeMillis();
		cnt=0;
		values[0]=null;values[1]=null;
		sensorData.clear();
    }
    
    public void onDestroy(){
		super.onDestroy();

		if(sensorManager != null){
			sensorManager.unregisterListener(myListener);
		}
		Analysis.time=0;
		Analysis.train_x=new ArrayList<Double[]>();
		Analysis.train_y=new ArrayList<Double>();
		Intent intent=new Intent(context,MainActivity.class);
		startActivity(intent);
		
		
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
			if (flag==0 && values[0]!=null && values[1]!=null && Tuple.mod(new Tuple(values[0]))>Analysis.shakeJudgeStart) {
				flag=1;
			}
			if(flag==1 && values[0]!=null && values[1]!=null){
				if (Tuple.mod(new Tuple(values[0]))<Analysis.ShakeJudgeEnd) {
					flag=2;
					n++;
					if(n<80){
						Toast.makeText(context, "连续摇晃时间过短,请重新摇晃", Toast.LENGTH_SHORT).show();
						values[0]=null;
						values[1]=null;
						sensorData.clear();
						flag=0;
						n=0;
					}
					else {
						////////////////////////////////
						sensorData.add(new Tuple[]{new Tuple(values[0]),new Tuple(values[1])});
						timeEnded=System.currentTimeMillis();
						f=(cnt*1000.0)/(timeEnded-timeStarted);
						alertDialogForSubmit();
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

	private void alertDialogForSubmit(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("是否提交本次摇晃样本").setMessage("提示：样本摇晃方式需保持一致且摇晃时间不能太短,否则会影响解锁效果");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				values[0]=null;
				values[1]=null;
				ArrayList<Tuple[]> temp=new ArrayList<Tuple[]>();
				temp.addAll(sensorData);
				Analysis.init(context,msgHandler,f);
				Analysis.receiveData(temp);
				sensorData.clear();
				flag=0;
				n=0;
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				values[0]=null;
				values[1]=null;
				sensorData.clear();
				flag=0;
				n=0;
			}
		});
		builder.setCancelable(false);
		if(!isFinishing()){
			builder.create().show();
		}
	}
	private Handler msgHandler= new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==1){
				Size.animate(msg.arg1);
				if (msg.arg1>=40) {
					Toast.makeText(context, "摇晃密码设置成功", Toast.LENGTH_SHORT).show();
					if(sensorManager != null){
						sensorManager.unregisterListener(myListener);
					}
					Intent intent=new Intent(context,WaitActivity.class);
					startActivity(intent);
					//finish();
				}
				
			}
			
		}
	};
}
