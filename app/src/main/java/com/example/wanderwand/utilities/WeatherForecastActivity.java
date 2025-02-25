package com.example.wanderwand.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.wanderwand.R;
import com.example.wanderwand.destinations.description.WeatherActivity;
import com.example.wanderwand.searchcitydialog.CitySearchBottomSheetDialogFragment;
import com.example.wanderwand.searchcitydialog.CitySearchModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.Constants.API_LINK_V2;
import static utils.Constants.USER_TOKEN;

public class WeatherForecastActivity extends AppCompatActivity {

    @BindView(R.id.select_city)
    Button selectCity;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    private ArrayList<CitySearchModel> mSearchCities = new ArrayList<>();
    private String mToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities_weather_forecast);

        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = sharedPreferences.getString(USER_TOKEN, null);

        fetchCitiesList();

        selectCity.setOnClickListener(v -> showSearchDialog());

        setTitle("Weather Forecast");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Called when user clicks on Select City
     * Displays a Search Dialog
     */
    private void showSearchDialog() {
        CitySearchBottomSheetDialogFragment citySearchBottomSheetDialogFragment =
                CitySearchBottomSheetDialogFragment.newInstance(R.string.search_title, R.string.search_hint);
        citySearchBottomSheetDialogFragment.setmCitySearchModels(mSearchCities);
        citySearchBottomSheetDialogFragment.setmListener(position -> {
            CitySearchModel item = mSearchCities.get(position);
            Intent intent = WeatherActivity.getStartIntent(WeatherForecastActivity.this, item.getName(),
                    item.getId(), true);
            startActivity(intent);
            citySearchBottomSheetDialogFragment.dismissAllowingStateLoss();
        });
        citySearchBottomSheetDialogFragment.show(getSupportFragmentManager(), "CitySearch");
    }

    /**
     * Fetches cities to be displayed in search list
     */
    private void fetchCitiesList() {

        Handler handler = new Handler(Looper.getMainLooper());
        String uri = API_LINK_V2 + "get-all-cities/10";
        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                handler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                handler.post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String res = response.body().string();
                            Log.v("RESULT", res);
                            JSONArray ar = new JSONArray(res);
                            for (int i = 0; i < ar.length(); i++) {
                                mSearchCities.add(new CitySearchModel(
                                        ar.getJSONObject(i).getString("city_name"),
                                        ar.getJSONObject(i).optString("image"),
                                        ar.getJSONObject(i).getString("id")));
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            networkError();
                            Log.e("ERROR", "Message : " + e.getMessage());
                        }
                    } else {
                        Log.e("ERROR", "Network error");
                        networkError();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Plays network lost animation
     */
    private void networkError() {
        selectCity.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.network_lost);
        animationView.playAnimation();
    }

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, WeatherForecastActivity.class);
        return intent;
    }
}
