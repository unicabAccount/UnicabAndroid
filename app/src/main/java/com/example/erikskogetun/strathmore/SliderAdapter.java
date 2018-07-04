package com.example.erikskogetun.strathmore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SliderAdapter extends PagerAdapter{
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter (Context context) {
        this.context = context;
    }

//    Arrays
    public int[] slideImages = {
        R.drawable.taxi_image,
        R.drawable.taxi_image,
        R.drawable.taxi_image
    };

    public String[] slide_headings = {
            "Share rides with friends",
            "Reduce transport costs",
            "Travel more conveniently"
    };

    public String[] slide_descs = {
            "Unicab allows you to share your ride with friends or colleagues within the Strathmore community",
            "With Unicab, you can now cut down your daily transport costs by up to 70%",
            "Unicab makes transport convenient for you and for others by making transport more efficient"
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object ;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDescription = view.findViewById(R.id.slide_desc);

        slideImageView.setImageResource(slideImages[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        container.addView(view);

        return view;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((RelativeLayout)object);
    }
}
