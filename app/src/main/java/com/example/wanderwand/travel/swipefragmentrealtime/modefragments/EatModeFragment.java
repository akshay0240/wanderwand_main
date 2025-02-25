package com.example.wanderwand.travel.swipefragmentrealtime.modefragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.wanderwand.R;
import com.example.wanderwand.travel.swipefragmentrealtime.MapListItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import objects.MapItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.Constants.API_LINK_V2;
import static utils.Constants.HERE_API_MODES;

public class EatModeFragment extends Fragment {


    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    private static String mCurlat;
    private static String mCurlon;
    private Context mContext;
    private static String mToken;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private List<MapItem> mMapItems =  new ArrayList<>();

    public EatModeFragment() {
        //required public constructor
    }

    public static EatModeFragment newInstance(String currentLat, String currentLon, String token) {
        mCurlat = currentLat;
        mCurlon = currentLon;
        mToken = token;
        return new EatModeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_each_list_real_time, container, false);
        ButterKnife.bind(this, rootView);
        getPlaces();
        return rootView;
    }

    /**
     * Gets all nearby places to eat at
     */
    private void getPlaces() {
        Handler handler = new Handler(Looper.getMainLooper());

        String uri = API_LINK_V2 + "get-places/" + mCurlat + "/" + mCurlon
                + "/" + HERE_API_MODES.get(0);

        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
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
            public void onResponse(final Call call, final Response response) throws IOException {

                final String res = Objects.requireNonNull(response.body()).string();
                handler.post(() -> {
                    try {
                        JSONArray routeArray = new JSONArray(res);

                        for (int i = 0; i < routeArray.length(); i++) {
                            String name = routeArray.getJSONObject(i).getString("title");
                            String web = routeArray.getJSONObject(i).getString("icon");
                            String number = routeArray.getJSONObject(i).getString("distance");
                            String address = routeArray.getJSONObject(i).getString("address");

                            mMapItems.add(new MapItem(name, number, web, address));
                        }
                        animationView.setVisibility(View.GONE);
                        listView.setAdapter(new MapListItemAdapter(mContext, mMapItems));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        noResult();
                        Log.e("ERROR : ", e.getMessage() + " ");
                    }
                });
            }
        });
    }

    /**
     * Plays the network lost animation in the view
     */
    private void networkError() {
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.network_lost);
        animationView.playAnimation();
    }

    /**
     * Plays the no data found animation in the view
     */
    private void noResult() {
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.empty_list);
        animationView.playAnimation();
    }
}
