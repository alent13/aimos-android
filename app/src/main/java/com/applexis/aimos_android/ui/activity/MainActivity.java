package com.applexis.aimos_android.ui.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.applexis.aimos_android.R;
import com.applexis.aimos_android.network.AimosAPI;
import com.applexis.aimos_android.network.AimosAPIClient;
import com.applexis.aimos_android.ui.adapter.MainViewPagerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity {

    private static AimosAPI aimosAPI = AimosAPIClient.getClient().create(AimosAPI.class);
    @BindView(R.id.main_viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {
        viewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager()));

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_my_page),
                        ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(getString(R.string.home))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_contacts),
                        ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(getString(R.string.contacts))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_dialogs),
                        ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(getString(R.string.dialogs))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_bell),
                        ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(getString(R.string.notifications))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_storage),
                        ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .title(getString(R.string.storage))
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 4);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
    }
}
