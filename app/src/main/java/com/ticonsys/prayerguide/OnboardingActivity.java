package com.ticonsys.prayerguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.ticonsys.prayerguide.fragments.OnOnboardingOptionSelectedListener;
import com.ticonsys.prayerguide.fragments.OnboardingAdjustmentHighLatitudesFragment;
import com.ticonsys.prayerguide.fragments.OnboardingAsrCalculationMethodFragment;
import com.ticonsys.prayerguide.fragments.OnboardingCalculationMethodFragment;
import com.ticonsys.prayerguide.fragments.OnboardingTimeFormatFragment;
import com.ticonsys.prayerguide.util.AppSettings;
import com.ticonsys.prayerguide.util.ScreenUtils;
import com.ticonsys.prayerguide.widget.FragmentStatePagerAdapter;

public class OnboardingActivity extends AppCompatActivity implements OnOnboardingOptionSelectedListener {

  public static final String EXTRA_CARD_INDEX = "card_index";

  private ViewPager mPager;
  private PagerAdapter mPagerAdapter;

  private int mCardIndex = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

    ScreenUtils.lockOrientation(this);
    setContentView(R.layout.activity_onboarding);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    //getSupportActionBar().setDisplayShowTitleEnabled(false);
   // android.app.ActionBar actionBar = getActionBar();

   // actionBar.setDisplayHomeAsUpEnabled(true);
   // actionBar.setHomeButtonEnabled(true);
    // Instantiate a ViewPager and a PagerAdapter.
    Intent intent = getIntent();
    mCardIndex = intent.getIntExtra(EXTRA_CARD_INDEX, 0);
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mCardIndex);
    mPager.setAdapter(mPagerAdapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // app icon in action bar clicked; goto parent activity.
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }


  @Override
  public void onBackPressed() {
    if (mPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else {
      // Otherwise, select the previous step.
      mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    }
  }

  @Override
  public void onOptionSelected() {
    if (mPager.getCurrentItem() + 1 == mPagerAdapter.getCount()) {
      AppSettings.getInstance(this).set(AppSettings.Key.HAS_DEFAULT_SET, true);
      Intent data = new Intent();
      if (getParent() == null) {
        setResult(RESULT_OK, data);
      } else {
        getParent().setResult(RESULT_OK, data);
      }
      finish();
    } else {
      mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }
  }

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    int mCardIndex = 0;

    public ScreenSlidePagerAdapter(FragmentManager fm, int cardIndex) {
      super(fm);
      mCardIndex = cardIndex;
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return OnboardingCalculationMethodFragment.newInstance(mCardIndex);
        case 1:
          return OnboardingAsrCalculationMethodFragment.newInstance(mCardIndex);
        case 2:
          return OnboardingAdjustmentHighLatitudesFragment.newInstance(mCardIndex);
        case 3:
          return OnboardingTimeFormatFragment.newInstance(mCardIndex);
      }
      return null;
    }

    @Override
    public int getCount() {
      return 4;
    }
  }

}
