package com.shakein;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;




public class Adapter extends FragmentPagerAdapter {
    private static final int COUNT = 1;

    public Adapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return Size.getInstance();
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
