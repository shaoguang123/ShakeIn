package com.shakein;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.glomadrian.dashedcircularprogress.DashedCircularProgress;
import com.shakein.R.color;



public class Size extends Fragment {

    private static DashedCircularProgress dashedCircularProgress;
    private ImageView androidImage;
    private TextView num;
    int m=0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.size, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashedCircularProgress = (DashedCircularProgress) view.findViewById(R.id.size);
        androidImage = (ImageView) view.findViewById(R.id.android_image);
        dashedCircularProgress.setInterpolator(new AccelerateInterpolator());
        dashedCircularProgress.setValue(m);
        num=(TextView)view.findViewById(R.id.num);
        /*restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate();
            }
        });*/

        dashedCircularProgress.setOnValueChangeListener(new DashedCircularProgress.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {
                androidImage.setScaleX(dashedCircularProgress.getScaleX() + 5 / 3);
                androidImage.setScaleY(dashedCircularProgress.getScaleY() + 5 / 3);
                num.setText((int) value + "%");
            }
        });

    }

    public static void animate(int n) {
        //dashedCircularProgress.reset();
    	if (n<40) {
    		n=100*n/40;
		}else {
			n=100;
		}
        dashedCircularProgress.setValue(n);
    }

    public static Size getInstance() {
        return new Size();
    }
}