package com.example.blanche.go4lunch.utils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.blanche.go4lunch.fragments.PageFragment;
import com.example.blanche.go4lunch.fragments.SecondPageFragment;
import com.example.blanche.go4lunch.fragments.ThirdPageFragment;

public class PageAdapter extends FragmentPagerAdapter {

    private String[] texts;

    public PageAdapter(FragmentManager fm, String[] texts) {
        super(fm);
        this.texts = texts;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PageFragment.newInstance(position);
            case 1:
                return SecondPageFragment.newInstance(position);
            case 2:
                return ThirdPageFragment.newInstance(position);
                default:
                    return PageFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return texts[position];
    }
}
