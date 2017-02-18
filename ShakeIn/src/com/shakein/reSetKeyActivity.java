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

public class reSetKeyActivity extends Activity{
	
	private Button button31;
	private Button button32;
	private EditText key31;
	private EditText key32;
	private EditText key33;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.key3);
		button31=(Button)findViewById(R.id.button_back_key3);
		button32=(Button)findViewById(R.id.button_confirm_key3);
		key31=(EditText)findViewById(R.id.old_password_key3);
		key32=(EditText)findViewById(R.id.new_password_key3);
		key33=(EditText)findViewById(R.id.confirm_password_key3);
		
		button31.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		button32.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String word1=key31.getText().toString();
				String word2=key32.getText().toString();
				String word3=key33.getText().toString();
				SharedPreferences pref=getSharedPreferences("ShakeIn",MODE_PRIVATE);
				String word= pref.getString("word", "");
				if(word1==null || word1.length()==0){
					Toast.makeText(reSetKeyActivity.this, "口令不能为空", Toast.LENGTH_SHORT).show();
				}else if (word.equals(word1)) {
					if(word2==null || word3==null || word3.length()==0 || word2.length()==0){
						Toast.makeText(reSetKeyActivity.this, "口令不能为空", Toast.LENGTH_SHORT).show();
					}else if (word3.equals(word2)) {
						SharedPreferences.Editor editor= getSharedPreferences("ShakeIn", MODE_PRIVATE).edit();
						editor.putString("word", word2);
						editor.commit();
						Toast.makeText(reSetKeyActivity.this, "口令设置成功", Toast.LENGTH_SHORT).show();
						//Intent intent3=new Intent(setKeyActivity.this,lockService.class);
						//startService(intent3);
						
						finish();
					}else {
						Toast.makeText(reSetKeyActivity.this, "新口令前后不一致", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(reSetKeyActivity.this, "原口令不正确", Toast.LENGTH_SHORT).show();
				}
				
				
			}
		});
	}
}
