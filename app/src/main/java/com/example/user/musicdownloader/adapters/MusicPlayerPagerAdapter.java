package com.example.user.musicdownloader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.user.musicdownloader.fragments.fragmentSongPlayer;

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

        return fragmentSongPlayer.newInstance(position);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 5;
    }

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
                return "Folders";
            case 4:
                return "Playlist";
        }
        return null;
    }
}