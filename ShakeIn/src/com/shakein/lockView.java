package com.shakein;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class lockView extends RelativeLayout{
	private View rootView;
	private int width;
	private Context mContext;
	
	public lockView(Context context) {
		super(context);
		mContext=context;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = inflater.inflate(R.layout.lock,this);
		
		// TODO Auto-generated constructor stub
	}

}
