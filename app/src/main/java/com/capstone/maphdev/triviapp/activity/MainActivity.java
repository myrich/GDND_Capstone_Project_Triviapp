package com.capstone.maphdev.triviapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import com.capstone.maphdev.triviapp.R;
import com.capstone.maphdev.triviapp.fragment.CategoriesListFragment;
import com.capstone.maphdev.triviapp.fragment.StatsFragment;
import com.capstone.maphdev.triviapp.utils.DesignUtils;
import com.capstone.maphdev.triviapp.utils.NetworkUtils;
import com.capstone.maphdev.triviapp.QuizWidgetProvider;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    @BindView(R.id.categories_list) FrameLayout container;
    @BindView(R.id.viewPager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Firebase auth
        auth = FirebaseAuth.getInstance();

        // if the user is not logged in anymore, then redirection to the Welcome Activity
        if (auth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        // check if there is an internet connection
        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())){
            DesignUtils.showSnackBar(container, getResources().getString(R.string.no_internet_connection), getApplicationContext());
        }

        // set Adapter for categories list
        setAdapter(1);

        // if a widget is already on screen and there is an internet connection, it gets updated
        if (NetworkUtils.isNetworkAvailable(getApplicationContext())){
            QuizWidgetProvider.sendBroadCast(this, QuizWidgetProvider.class);
        }
    }

    private void setAdapter(int position){
        FragmentPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new StatsFragment();
                case 1:
                    return new CategoriesListFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    public void signOut(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("shared", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("shared_score", -1);
        editor.putLong("shared_nb_questions", -1);
        editor.apply();
        QuizWidgetProvider.sendBroadCast(getApplicationContext(), QuizWidgetProvider.class);
        auth.signOut();
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
