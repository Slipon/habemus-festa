package com.example.habemusfesta.ui.main;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.habemusfesta.events.MyEvents;
import com.example.habemusfesta.events.NearByEvents;
import com.example.habemusfesta.R;
import com.example.habemusfesta.events.RecentEvents;
import com.example.habemusfesta.events.TrendingEvents;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.text_event_near_you, R.string.text_event_trending, R.string.text_event_recent, R.string.text_event_my_events};
    private final Context mContext;
    private Fragment currentFragment;
    private String currentTag = "";
    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public Fragment getCurrentFragment(){
        return this.currentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public void setCurrentFilter(String tag){
        this.currentTag = tag;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new NearByEvents(this.currentTag);
                break;
            case 1:
                fragment = new TrendingEvents(this.currentTag);
                break;
            case 2:
                fragment = new RecentEvents(this.currentTag);
                break;
            case 3:
                fragment = new MyEvents(this.currentTag);
                break;
        }
        return fragment;
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        //return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 4;
    }
}