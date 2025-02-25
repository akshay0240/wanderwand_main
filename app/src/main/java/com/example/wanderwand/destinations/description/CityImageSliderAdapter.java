package com.example.wanderwand.destinations.description;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.wanderwand.FullScreenImage;
import com.example.wanderwand.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CityImageSliderAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> mImagesArray;
    private String mCityName;
    CityImageSliderAdapter(Context context, ArrayList<String> imagesArray, String cityName) {
        this.mContext = context;
        this.mImagesArray = imagesArray;
        this.mCityName = cityName;
    }
    @Override
    public int getCount() {
        return mImagesArray.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        //display default city image if no image is present in array
        if (mImagesArray.size() == 0) {
            Picasso.get().load(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image).fit().centerCrop().into(imageView);
        }
        Picasso.get().load(mImagesArray.get(position))
                .error(R.drawable.placeholder_image).fit().centerCrop().into(imageView);
        container.addView(imageView);
        imageView.setOnClickListener(v -> {
            Intent fullScreenIntent = FullScreenImage.getStartIntent(mContext,
                    mImagesArray.get(position), mCityName);
            mContext.startActivity(fullScreenIntent);
        });
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setAlpha(0.6f);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
