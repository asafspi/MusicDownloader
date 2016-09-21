package com.example.user.musicdownloader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.user.musicdownloader.fragments.FragmentSearch;


public class SearchOnlinePagerAdapter extends FragmentPagerAdapter {

    public SearchOnlinePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentSearch.newInstance();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Search";
        }
        return null;
    }
}