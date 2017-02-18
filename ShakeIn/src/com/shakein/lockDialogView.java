package com.shakein;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class lockDialogView extends LinearLayout{
	
	private View rootView;
	private int width;
	private EditText editText;
	private Button button1;
	private Button button2;
	private Context mContext;

	public lockDialogView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext=context;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = inflater.inflate(R.layout.lock_dialog,this);
		initViews();
	}
	
	private void initViews(){
		editText=(EditText)findViewById(R.id.password_lock_dialog);
		button1=(Button)findViewById(R.id.button1_log_dialog);
		button2=(Button)findViewById(R.id.button2_log_dialog);
		
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String pass=editText.getText().toString();
				SharedPreferences pref=mContext.getSharedPreferences("ShakeIn",Context.MODE_PRIVATE);
				String password= pref.getString("word", "");
				if (pass.equals(password)) {
					Intent i1 = new Intent(mContext,lockService.class);
					i1.setAction(lockService.RemoveView2);
					mContext.startService(i1);
					Intent i = new Intent(mContext,lockService.class);
					i.setAction(lockService.UNLOCK_ACTION);
					mContext.startService(i);
					
				} else {
					Toast.makeText(mContext, "¿ÚÁî´íÎó", Toast.LENGTH_SHORT).show();
					VibratorUtil.Vibrate(mContext, 200);
				}
				
			}
		});
		button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(mContext,lockService.class);
				i.setAction(lockService.RemoveView2);
				mContext.startService(i);
				
			}
		});
		
	}

}
