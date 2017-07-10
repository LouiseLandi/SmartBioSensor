package com.application.smartbiosensor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                MeasureFragment measureFragment = new MeasureFragment();
                return measureFragment;
            case 1:
                DetailFragment detailFragment = new DetailFragment();
                return detailFragment;
            case 2:
                CalibrationFragment calibrationFragment = new CalibrationFragment();
                return calibrationFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}