package com.shakein;

import android.app.Activity;  
import android.app.Service;  
import android.content.Context;
import android.os.Vibrator;  
  
/** 
 * �ֻ��𶯹����� 
 * @author Administrator 
 * 
 */  
public class VibratorUtil {  
      
    /** 
     * final Activity activity  �����ø÷�����Activityʵ�� 
     * long milliseconds ���𶯵�ʱ������λ�Ǻ��� 
     * long[] pattern  ���Զ�����ģʽ �����������ֵĺ���������[��ֹʱ������ʱ������ֹʱ������ʱ��������]ʱ���ĵ�λ�Ǻ��� 
     * boolean isRepeat �� �Ƿ񷴸��𶯣������true�������𶯣������false��ֻ��һ�� 
     */  
      
     public static void Vibrate(final Activity activity, long milliseconds) {   
            Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);   
            vib.vibrate(milliseconds);   
     }   
     public static void Vibrate(final Context activity, long milliseconds) {   
            Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);   
            vib.vibrate(milliseconds);  
     }   
  
}
