package com.example.wanderwand.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderwand.R;

import java.util.ArrayList;
import java.util.List;

import adapters.CardViewOptionsAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import utils.CardItemEntity;

public class UtilitiesFragment extends Fragment implements CardViewOptionsAdapter.OnItemClickListener {

    @BindView(R.id.utility_options_recycle_view)
    RecyclerView mUtilityOptionsRecycleView;
    private AppCompatActivity mActivity;
    private boolean mHasMagneticSensor = true;

    public UtilitiesFragment() {
    }

    public static UtilitiesFragment newInstance() {
        UtilitiesFragment fragment = new UtilitiesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_utility, container, false);

        ButterKnife.bind(this, view);

        List<CardItemEntity> cardEntities = getUtilityItems();

        CardViewOptionsAdapter cardViewOptionsAdapter = new CardViewOptionsAdapter(this, cardEntities);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        mUtilityOptionsRecycleView.setLayoutManager(mLayoutManager);
        mUtilityOptionsRecycleView.setItemAnimator(new DefaultItemAnimator());
        mUtilityOptionsRecycleView.setAdapter(cardViewOptionsAdapter);

        PackageManager mManager = getActivity().getPackageManager();
        boolean hasAccelerometer = mManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        boolean hasMagneticSensor = mManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
        if (!hasAccelerometer || !hasMagneticSensor) {
            this.mHasMagneticSensor = false;
        }

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onItemClick(int position) {
        Intent intent;
        switch (position) {
            case 0:
                intent = ChecklistActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
            case 1:
                intent = WeatherForecastActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
            case 2:
                intent = CompassActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
            case 3:
                intent = CurrencyActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
            case 4:
                intent = WorldClockActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
            case 5:
                intent = UpcomingWeekendsActivity.getStartIntent(mActivity);
                startActivity(intent);
                break;
        }
    }

    private List<CardItemEntity> getUtilityItems() {
        List<CardItemEntity> cardEntities = new ArrayList<>();
        cardEntities.add(
                new CardItemEntity(
                        getActivity().getDrawable(R.drawable.checklist),
                        getResources().getString(R.string.text_checklist)));
        cardEntities.add(
                new CardItemEntity(
                        getActivity().getDrawable(R.drawable.weather),
                        getResources().getString(R.string.text_weather)));
        if (mHasMagneticSensor) {
            cardEntities.add(
                    new CardItemEntity(
                            getActivity().getDrawable(R.drawable.compass),
                            getResources().getString(R.string.text_Compass)));
        }
        cardEntities.add(
                new CardItemEntity(
                        getActivity().getDrawable(R.drawable.currency),
                        getResources().getString(R.string.text_currency)));
        cardEntities.add(
                new CardItemEntity(
                        getActivity().getDrawable(R.drawable.worldclock),
                        getResources().getString(R.string.text_clock)));
        cardEntities.add(
                new CardItemEntity(
                        getActivity().getDrawable(R.drawable.upcoming_long_weekends),
                        getResources().getString(R.string.upcoming_long_weekends)));
        if (!mHasMagneticSensor) {
            cardEntities.add(
                    new CardItemEntity(
                            getActivity().getDrawable(R.drawable.compass_unavailable),
                            getResources().getString(R.string.text_Compass)));
        }
        return cardEntities;
    }
}
