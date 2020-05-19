package com.example.buzzr.HelperClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.example.buzzr.R;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    int[] images = {
            R.drawable.trial,
            R.drawable.trial,
            R.drawable.trial
    };
    int[] titles = {
            R.string.obTitle1, R.string.obTitle2, R.string.obTitle3
    };
    int[] des = {
            R.string.obText1, R.string.obText2, R.string.obText3
    };

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.ob_slides_layout, container, false);

        //Hooks
        ImageView image = view.findViewById(R.id.obSliderImage);
        TextView obTitle = view.findViewById(R.id.obSliderTitle);
        TextView obDes = view.findViewById(R.id.obSliderDes);

        image.setImageResource(images[position]);
        obTitle.setText(titles[position]);
        obDes.setText(des[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
