package com.shakein;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import libsvm.svm;
import android.R.integer;
import android.R.string;
import android.app.Fragment.SavedState;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.content.SharedPreferences.Editor;

public class Analysis {
	public static ArrayList<Tuple[]> currentReceiving;
	public static  Context context;
	public static double shakeJudgeStart=15;
	public static double ShakeJudgeEnd=5;
	public static double f;
	public static int time=0;
	public static  Handler msgHandler;
	public static ArrayList<Double[]> train_x = new ArrayList<Double[]>();
	public static ArrayList<Double> train_y = new ArrayList<Double>();
	public static svm_train my_train;

	
	public static void init(Context context,Handler msgHandler,double f){
		Analysis.context=context;
		Analysis.msgHandler=msgHandler;
		Analysis.currentReceiving=new ArrayList<Tuple[]>();
		Analysis.f=f;
	
	}
	public static void receiveData(ArrayList<Tuple[]> sensorData){
		currentReceiving.addAll(sensorData);
		


		final ArrayList<Tuple[]> submitTuples=new ArrayList<Tuple[]>();
		submitTuples.addAll(currentReceiving);
		final double data_f=Analysis.f;
		new Thread(new Runnable() {
				
			@Override
			public void run() {
				try {
					dataProcessing(submitTuples,data_f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();		
		currentReceiving.clear();
	}
	
	private static void store(ArrayList<Tuple[]> list){
		//n++;
		String m="";
		for(int i=0;i<(list.size());i++){
			m+=""+list.get(i)[0].x+"\t"+list.get(i)[0].y+"\t"+list.get(i)[0].z+"\t"+list.get(i)[1].x+"\t"+list.get(i)[1].y+"\t"+list.get(i)[1].z+"\n";
			//save(m,"data"+n);
		}
		writeSDcard(m,"data0.txt");
	}
	public static void writeSDcard(String str,String txt) {
        try {
            // 判断是否存在SD卡
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                File sdDire = Environment.getExternalStorageDirectory();
                FileOutputStream outFileStream = new FileOutputStream(
                        sdDire.getCanonicalPath() + "/shakeIn/"+txt);
                outFileStream.write(str.getBytes());
                outFileStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public static void save(String input,String file){
		FileOutputStream out=null;
		BufferedWriter writer=null;
		try {
			out=context.openFileOutput(file,Context.MODE_PRIVATE);
			writer=new BufferedWriter(new OutputStreamWriter(out));
			writer.write(input);
		} catch (IOException e) {
			e.printStackTrace();// TODO: handle exception
		}finally{
			try {
				if(writer!=null){
					writer.close();
				}
			} catch (IOException e2) {
				e2.printStackTrace();// TODO: handle exception
			}
		}  
	}
	private static void  returnResult(int result,int arg) {
		Message msg=new Message();
		msg.arg1=arg;
		msg.what=result;
		msgHandler.sendMessage(msg);
	}
	///////////////////////////数据处理部分////////////////////////////
	public static void dataProcessing(ArrayList<Tuple[]> list,double f) throws IOException{
		int shakeTimes=0;
		
		int n=list.size();
		double t=1/f;
		ArrayList<Double> x=new ArrayList<Double>();
		ArrayList<Double> y=new ArrayList<Double>();
		ArrayList<Double> w=new ArrayList<Double>();
		ArrayList<Double> v=new ArrayList<Double>();

		for(int i=0;i<n;i++){
			x.add((double) list.get(i)[0].x);
			y.add((double) list.get(i)[0].y);
			w.add((double) list.get(i)[1].z);
		}
		
		x=MovingAverageFilter(x,5);
		y=MovingAverageFilter(y,5);
		w=MovingAverageFilter(w,5);
		
		ArrayList<Integer> zeros=findZreo(w);
		int zerosSize=zeros.size();
		int times=(zeros.size()-1)/2;
		synchronized (Analysis.class) {
			shakeTimes=time+times;
			time=shakeTimes;
			returnResult(1, time);
	    }
		
		int xSize=x.size();
		for(int i=zeros.get(zerosSize-1)+1;i<xSize;i++){
			x.remove(zeros.get(zerosSize-1)+1);
			y.remove(zeros.get(zerosSize-1)+1);
			w.remove(zeros.get(zerosSize-1)+1);
		}
		for(int i=0;i<zeros.get(0);i++){
			x.remove(0);
			y.remove(0);
			w.remove(0);
		}
		
		ArrayList<Integer> zero=new ArrayList<Integer>();
		for(int i=0;i<zerosSize;i++){
			zero.add(zeros.get(i)-zeros.get(0));
		}
		ArrayList<Double> v_x=Integral(x, t);
		ArrayList<Double> v_y=Integral(y, t);
		v_x=CorrectedError(v_x, zero);
		v_y=CorrectedError(v_y, zero);
		for(int i=0;i<v_x.size();i++){
			double temp=Math.sqrt(v_x.get(i)*v_x.get(i)+v_y.get(i)*v_y.get(i));
			if (w.get(i)<0){
				temp=temp*(-1);
			}
			v.add(temp);
		}
		
		ArrayList<Double[]> vList=reSample1(v,zero);
		ArrayList<Double[]> wList=reSample1(w,zero);
		ArrayList<Double[]> r_up=new ArrayList<Double[]>();
		ArrayList<Double[]> r_down=new ArrayList<Double[]>();
		ArrayList<Double[]> v_up=new ArrayList<Double[]>();
		ArrayList<Double[]> v_down=new ArrayList<Double[]>();
		ArrayList<Double[]> w_up=new ArrayList<Double[]>();
		ArrayList<Double[]> w_down=new ArrayList<Double[]>();
		radius(vList,wList,zero.size(),r_up,r_down);
		int cnt1=0,cnt2=0;
		for(int i=0;i<zero.size()-1;i++){
			if(i%2==0){
				v_up.add(cnt1,vList.get(i));
				w_up.add(cnt1,wList.get(i));
				cnt1++;
			}else {
				v_down.add(cnt2,vList.get(i));
				w_down.add(cnt2,wList.get(i));
				cnt2++;
			}
		}
		
		for(int i=0;i<times;i++){
			normalization(v_up.get(i));
			normalization(v_down.get(i));
			normalization(w_up.get(i));
			normalization(w_down.get(i));
			normalization(r_up.get(i));
			normalization(r_down.get(i));
			Double[][] temp=new Double[times][1200];
			for(int j=0;j<200;j++){
				temp[i][j]=v_up.get(i)[j];
				temp[i][j+200]=v_down.get(i)[j];
				temp[i][j+400]=w_up.get(i)[j];
				temp[i][j+600]=w_down.get(i)[j];
				temp[i][j+800]=r_up.get(i)[j];
				temp[i][j+1000]=r_down.get(i)[j];
			}
			synchronized (Analysis.class) {
				train_x.add(temp[i]);
				train_y.add(1.0);
		    }
			
		}
		if(train_y.size()>=40){
			SharedPreferences ShakeInPref = context.getSharedPreferences(
					"ShakeIn", 0);
			boolean b = ShakeInPref.getBoolean("isFirstRun", true);
			Editor editor=ShakeInPref.edit();
			editor.putInt("times", train_y.size());
			final int temp=train_y.size();
			time=0;
			for(int i=0;i<temp-1;i++){
				for(int j=0;j<1200;j++){
					train_x.get(i)[j]=(train_x.get(i)[j]+train_x.get(i+1)[j])/2;
				}
			}
			for(int j=0;j<1200;j++){
				train_x.get(temp-1)[j]=(train_x.get(temp-1)[j]+train_x.get(0)[j])/2;
			}
			String[] option={"-s","2","-n","0.3"};
			String sd_card = Environment.getExternalStorageDirectory().toString();
		    String path = sd_card + "/shakeIn";
		    String model_name = path + "/my_model.txt";
		    String train_path = path + "/data0.txt";
						
			my_train = new svm_train();
			my_train.model_file_name=model_name;
			my_train.my_parse_command_line(option);
			my_train.my_read_problem(train_x,train_y);
			my_train.my_run();
			final ArrayList<Double[]> train_x_store = new ArrayList<Double[]>();
			train_x_store.addAll(train_x);
			train_x=new ArrayList<Double[]>();
			train_y=new ArrayList<Double>();
			Log.e("end", ""+train_x_store.get(train_x_store.size()-1)[1199]);
			myStore(train_path,train_x_store);
			Log.e("end", ""+train_x_store.get(train_x_store.size()-1)[1199]);
			ActivityCollector.finishAll();
			
			

			
		}

			
	}
	public static void myStore(String train_path,ArrayList<Double[]> train_x_store)throws IOException
	{
		DataOutputStream fp=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(train_path)));
		for(int i=0;i<train_x_store.size();i++){
			fp.writeBytes("1.0\t");
			for(int j=0;j<1200;j++){
				fp.writeBytes(""+(j+1)+":"+train_x_store.get(i)[j]+"\t");
			}
			fp.writeBytes("\n");
		}
		fp.close();
	}
	public static  ArrayList<Double>  MovingAverageFilter(ArrayList<Double>x,int windowSize){
		ArrayList<Double> x_new=new ArrayList<Double>();
		double temp=0;
		for (int i = 0; i < windowSize; i++) {
			temp+=x.get(i)/windowSize;
			x_new.add(temp);
		}
		for(int i=windowSize;i<x.size();i++){
			temp+=(x.get(i)-x.get(i-windowSize))/windowSize;
			x_new.add(temp);
		}
		return x_new;
	}
	
	public static ArrayList<Integer> findZreo(ArrayList<Double> w){
		ArrayList<Integer> index=new ArrayList<Integer>();
		for(int i=0;i<w.size()-1;i++){
			double temp=w.get(i)*w.get(i+1);
			if(temp<0){
				index.add(i);
			}
		}
		
		return index;
	}
	public static ArrayList<Double> Integral(ArrayList<Double> x,double t){
		ArrayList<Double> integralArrayList=new ArrayList<Double>();
		double temp=0;
		for(int i=0;i<x.size();i++){
			temp+=x.get(i)*t;
			integralArrayList.add(temp);
		}
		return integralArrayList;
	}
	public static ArrayList<Double> CorrectedError(ArrayList<Double> x,ArrayList<Integer> zero){
		ArrayList<Double> x_new=new ArrayList<Double>();
		ArrayList<Double> lingdianx=new ArrayList<Double>();
		ArrayList<Double> k=new ArrayList<Double>();
		for(int i=0;i<zero.size();i++){
			int ind=zero.get(i);
			lingdianx.add(x.get(ind));
		}
		for(int i=0;i<zero.size()-1;i++){
			double temp=(lingdianx.get(i+1)-lingdianx.get(i))/(zero.get(i+1)-zero.get(i));
			k.add(temp);
		}
		int j = 0;
		for(int i=0;i<zero.size()-1;i++){
			for(j=zero.get(i);j<zero.get(i+1);j++){
				double temp=x.get(j)-k.get(i)*(j-zero.get(i))-lingdianx.get(i);
				x_new.add(j, temp);
			}
		}
		x_new.add(j,0.0);
		return x_new;
		
		
	}
	public static ArrayList<Double[]> reSample1(ArrayList<Double> v,ArrayList<Integer> zero){
		ArrayList<Double[]> vList=new ArrayList<Double[]>();
		
		float[] v_in=new float[v.size()+1000];
		float[] v_out=new float[200*zero.size()+1000];
		for(int i=0;i<v.size();i++){
			v_in[i]=v.get(i).floatValue();
		}
		boolean flag=true;
		for(int i=0;i<zero.size()-1;i++){
			Double[] temp=new Double[200];
			double factor=200.0/(zero.get(i+1)-zero.get(i));
			Resampler resampler=new Resampler(false, factor, factor);/////////////////////////////////////////////////////////////////
			int inBufferOffset=zero.get(i);
			int inBufferLen=zero.get(i+1)-zero.get(i);
			int outBufferOffset=200*i;
			Resampler.Result result=resampler.process(factor, v_in, inBufferOffset, inBufferLen, flag, v_out, outBufferOffset, 200);
			

			for(int j=200*i;j<200*(i+1);j++){
				temp[j-200*i]=Double.valueOf(v_out[j]);
			}
			vList.add(temp);
		}
		
		return vList;
	}
	public static Double[] reSample2(Double[] x){
		Double[] y=new Double[200];
		float[] x_in=new float[x.length];
		float[] x_out=new float[200];
		for(int i=0;i<x.length;i++){
			x_in[i]=x[i].floatValue();
		}
		boolean flag=true;
		double factor=200.0/x.length;
		Resampler resampler=new Resampler(false, factor, factor);
		int inBufferOffset=0;
		int inBufferLen=x.length;
		int outBufferOffset=0;
		Resampler.Result result=resampler.process(factor, x_in, inBufferOffset, inBufferLen, flag, x_out, outBufferOffset, 200);
		for(int i=0;i<200;i++){
			y[i]=Double.valueOf(x_out[i]);
		}
		return y;
	}
	public static void radius(ArrayList<Double[]>v,ArrayList<Double[]>w,int n,ArrayList<Double[]>r_up,ArrayList<Double[]>r_down){
		Double[] temp=new Double[170];
		int cnt=0,cnt1=0,cnt2=0;
		for(int i=0;i<n-1;i++){
			double sum_w=0;
			double sum_v=0;
			cnt=0;
			for(int j=10;j<180;j++){
				if (j==10) {
					for(int k=0;k<10;k++){
						sum_w+=w.get(i)[j+k];
						sum_v+=v.get(i)[j+k];
					}
				}else{
					sum_w+=(w.get(i)[j+9]-w.get(i)[j-1]);
					sum_v+=(v.get(i)[j+9]-v.get(i)[j-1]);
				}
				temp[cnt]=Math.abs(sum_v/sum_w);
				cnt++;
			}
			
			if(i%2==0){
				temp=reSample2(temp);
				r_up.add(cnt1,temp);
				cnt1++;
			}else {
				temp=reSample2(temp);
				r_down.add(cnt2,temp);
				cnt2++;
			}
		}
		
	}
	public static void normalization(Double[] v){
		double max=0,min=0;
		for(int i=0;i<v.length;i++){
			if(v[i]>max) max=v[i];
			if(v[i]<min) min=v[i];
		}
		
		for(int i=0;i<v.length;i++){
			if(max==min) return;
			else if (v[i]==max) {
				v[i]=1.0;
			}else if (v[i]==min) {
				v[i]=-1.0;
			}else {
				v[i]=2*(v[i]-min)/(max-min)-1;
			}
			//Log.e("v["+i+"]", ""+v[i]);
		}
	}


	public static class double_holder{
		public double myAcc;
		public double_holder(double ac){
			myAcc=ac;
		}
	}
	

}
