package com.example.wanderwand.travel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.wanderwand.R;
import com.example.wanderwand.travel.swipefragmentrealtime.ViewPageFragmentsAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import utils.GPSTracker;
import utils.TravelmateSnackbars;

public class ListViewRealTimeActivity extends AppCompatActivity {

    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private static final int REQUEST_LOCATION = 199;
    private GPSTracker mTracker;
    private String mCurlat;
    private String mCurlon;
    public ArrayList<Integer> mSelectedIndices = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_real_time);
        ButterKnife.bind(this);

        // Get user's current location
        mTracker = new GPSTracker(this);
        MapView mMap = findViewById(R.id.action_map_view);
        if (!mTracker.canGetLocation()) {
            mTracker.displayLocationRequest(this, mMap);
        } else {
            mCurlat = Double.toString(mTracker.getLatitude());
            mCurlon = Double.toString(mTracker.getLongitude());
            mSelectedIndices.add(0);
        }

        ViewPageFragmentsAdapter adapter = new ViewPageFragmentsAdapter(this, getSupportFragmentManager(), 8,
                mCurlat, mCurlon);
        viewPager.setAdapter(adapter);

        // Connect the tab layout with the view pager
        // to enable updating of view pager on swipping and changing text
        // of tab
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle("List View");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_map_view :
                finish();
                Intent intent = MapViewRealTimeActivity.getStartIntent(ListViewRealTimeActivity.this);
                startActivity(intent);
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, ListViewRealTimeActivity.class);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case AppCompatActivity.RESULT_OK:
                        //User agreed to make required location settings changes
                        //startLocationUpdates();
                        TravelmateSnackbars.createSnackBar(findViewById(R.id.list_view_realtime),
                                R.string.location_enabled, Snackbar.LENGTH_LONG).show();
                        mCurlat = Double.toString(mTracker.getLatitude());
                        mCurlon = Double.toString(mTracker.getLongitude());
                        break;

                    case AppCompatActivity.RESULT_CANCELED:
                        //User chose not to make required location settings changes
                        TravelmateSnackbars.createSnackBar(findViewById(R.id.list_view_realtime),
                                R.string.location_not_enabled, Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }
}
