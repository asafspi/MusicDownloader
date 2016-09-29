package com.musicplayer.freedownload.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.musicplayer.freedownload.fragments.FragmentSearchTab;
import com.musicplayer.freedownload.fragments.fragmentSongPlayer;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MusicPlayerPagerAdapter extends FragmentPagerAdapter {

    public MusicPlayerPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 4){
            return FragmentSearchTab.newInstance();
        }
        return fragmentSongPlayer.newInstance(position);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 5;
    }

   /* @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }*/

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Songs";
            case 1:
                return "Artists";
            case 2:
                return "Albums";
            case 3:
                return "Downloads";
            case 4:
                return "Search";
        }
        return null;
    }
}