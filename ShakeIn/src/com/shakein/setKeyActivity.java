package com.shakein;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class setKeyActivity extends Activity{
	private Button button1;
	private Button button2;
	private EditText key11;
	private EditText key12;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.key4);
		button1=(Button)findViewById(R.id.button_back_key4);
		button2=(Button)findViewById(R.id.button2_key4);
		key11=(EditText)findViewById(R.id.password1_key4);
		key12=(EditText)findViewById(R.id.password2_key4);
		
		button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String word1=key11.getText().toString();
				String word2=key12.getText().toString();
				if(word1==null || word2==null || word1.length()==0 || word2.length()==0){
					Toast.makeText(setKeyActivity.this, "口令不能为空", Toast.LENGTH_SHORT).show();
				}else if (word1.equals(word2)) {
					SharedPreferences.Editor editor= getSharedPreferences("ShakeIn", MODE_PRIVATE).edit();
					editor.putString("word", word1);
					editor.commit();
					Toast.makeText(setKeyActivity.this, "口令设置成功", Toast.LENGTH_SHORT).show();
					//Intent intent3=new Intent(setKeyActivity.this,lockService.class);
					//startService(intent3);
					
					Intent intent=new Intent(setKeyActivity.this,sampleShakingActivity.class);
					startActivity(intent);
					
					finish();
				}else {
					Toast.makeText(setKeyActivity.this, "前后口令不一致", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	}
}
