package com.example.wanderwand.travel.swipefragmentrealtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.wanderwand.R;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.AtmModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.BusStopModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.EatModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.HangoutSpotFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.HospitalModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.MonumentsModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.PetrolModeFragment;
import com.example.wanderwand.travel.swipefragmentrealtime.modefragments.ShoppingModeFragment;

import static utils.Constants.USER_TOKEN;

/**
 *  An adapter that knows which fragment should be shown on each page
 */
public class ViewPageFragmentsAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private int mPagesCount;
    private String mCurrentLat;
    private String mCurrentLon;
    private String mToken;


    public ViewPageFragmentsAdapter(Context context, FragmentManager fm, int pagesCount,
                                    String currentLat, String currentLon) {
        super(fm);
        mContext = context;
        mPagesCount = pagesCount;
        mCurrentLat = currentLat;
        mCurrentLon = currentLon;
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return EatModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 1:
                return HangoutSpotFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 2:
                return MonumentsModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 3:
                return BusStopModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 4:
                return ShoppingModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 5:
                return PetrolModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 6:
                return AtmModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            case 7:
                return HospitalModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);
            default:
                return EatModeFragment.newInstance(mCurrentLat, mCurrentLon, mToken);

        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return mPagesCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.food);
            case 1:
                return mContext.getString(R.string.hangout);
            case 2:
                return mContext.getString(R.string.text_monuments);
            case 3:
                return mContext.getString(R.string.travelling);
            case 4:
                return mContext.getString(R.string.text_shopping);
            case 5:
                return mContext.getString(R.string.petrol);
            case 6:
                return mContext.getString(R.string.atm);
            case 7:
                return mContext.getString(R.string.hospital);
            default:
                return mContext.getString(R.string.food);
        }
    }
}
