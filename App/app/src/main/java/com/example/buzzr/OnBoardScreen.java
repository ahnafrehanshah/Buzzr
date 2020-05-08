package com.example.buzzr;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.buzzr.HelperClasses.SliderAdapter;
import com.example.buzzr.HelperClasses.sharedPrefs;

public class OnBoardScreen extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout dotsLayout;

    SliderAdapter sliderAdapter;
    TextView[] obSliderDots;

    Button lgsButton, signButton;

    // Animations
    Animation animation;

    int currentSlidePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.on_board);

        // Hooks
        viewPager = findViewById(R.id.obSlider);
        dotsLayout = findViewById(R.id.obDots);
        lgsButton = findViewById(R.id.obLgsBtn);
        signButton = findViewById(R.id.obSignBtn);

        //Adapter
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        sliderDots(0);
        viewPager.addOnPageChangeListener(changeListener);
    }

    public void skipSlide(View view) {
        viewPager.setCurrentItem(2);
    }

    public void nextSlide(View view) {
        viewPager.setCurrentItem(currentSlidePosition + 1);
    }

    private void sliderDots(int position) {
        obSliderDots = new TextView[3];
        dotsLayout.removeAllViews();

        for (int i = 0; i < obSliderDots.length; i++) {
            obSliderDots[i] = new TextView(this);
            obSliderDots[i].setText(Html.fromHtml("&#8226;"));
            obSliderDots[i].setTextSize(35);
            obSliderDots[i].setTextColor(getResources().getColor(R.color.colorAccent));

            dotsLayout.addView(obSliderDots[i]);
        }

        if (obSliderDots.length > 0) {
            obSliderDots[position].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            sliderDots(position);
            currentSlidePosition = position;

            if (position == 2) {
                animation = AnimationUtils.loadAnimation(OnBoardScreen.this, R.anim.ob_btnup);
                signButton.setAnimation(animation);
                lgsButton.setAnimation(animation);
                signButton.setVisibility(View.VISIBLE);
                lgsButton.setVisibility(View.VISIBLE);
            } else {
                signButton.setVisibility(View.INVISIBLE);
                lgsButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void openLogIn(View view) {
        sharedPrefs preference = new sharedPrefs(getApplicationContext());
        preference.setIsFirstTime(false);

        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }

    public void openSignUp(View view) {
        sharedPrefs preference = new sharedPrefs(getApplicationContext());
        preference.setIsFirstTime(false);

        startActivity(new Intent(this, SignUpScreen.class));
        finish();
    }
}
