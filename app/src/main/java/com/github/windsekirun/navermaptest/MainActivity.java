package com.github.windsekirun.navermaptest;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.nhn.android.maps.NMapActivity;

public class MainActivity extends NMapActivity {
    boolean isContentVisible = false;
    NMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        map = (NMap) findViewById(R.id.mainLayout);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 205, getResources().getDisplayMetrics()));
        map.setLayoutParams(params);

        map.setOnMapExpandListener(new NMap.OnMapExpandListener() {
            @Override
            public void onExpand(int which, Object data) {
                FrameLayout.LayoutParams paramsMatch = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 205, getResources().getDisplayMetrics()));

                isContentVisible = !isContentVisible;
                map.setLayoutParams(isContentVisible ? paramsMatch : params);
            }
        });
    }
}
