package com.example.erikskogetun.strathmore;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

//    Init view variables
    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;

    private TextView[] mDots;
    private SliderAdapter sliderAdapter;

    private Button mNextButton;
    private Button mBackButton;
    private Button mFinishButton;
    private int mCurrentPage;
    final private String TAG = "MainActivity";
    boolean lastPage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSlideViewPager =  findViewById(R.id.slideViewPager);
        mDotLayout = findViewById(R.id.dotsLayout);

        mNextButton = findViewById(R.id.nextButton);
        mBackButton = findViewById(R.id.previousButton);
        mFinishButton = findViewById(R.id.finishButton);
        sliderAdapter = new SliderAdapter(this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                    finish();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mSlideViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });
    }


    public void addDotsIndicator(int position){
        mDots = new TextView[3];
        mDotLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            mDotLayout.addView(mDots[i]);
        }

        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);

            mCurrentPage = position;

            if(position == 0) {
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(false);
                mBackButton.setVisibility(View.INVISIBLE);

                mNextButton.setText("Next");
                mBackButton.setText("");
            } else if (position == mDots.length -1) {
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);

//                mNextButton.setText("Finish");
                switchButton(true);
                mBackButton.setText("Back");
            } else {
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);

                switchButton(false);
                mNextButton.setText("Next");
                mBackButton.setText("Back");
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void switchButton(boolean showFinish){
        if (showFinish) {
        mFinishButton.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
        } else {
            mFinishButton.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
        }
    }
}
