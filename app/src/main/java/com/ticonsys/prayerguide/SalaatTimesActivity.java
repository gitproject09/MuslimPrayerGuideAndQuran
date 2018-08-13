package com.ticonsys.prayerguide;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.ticonsys.prayerguide.fragments.InitialConfigFragment;
import com.ticonsys.prayerguide.fragments.LocationHelper;
import com.ticonsys.prayerguide.fragments.OfflineQuranFragment;
import com.ticonsys.prayerguide.fragments.QiblaDirectionFragment;
import com.ticonsys.prayerguide.fragments.SalaatTimesFragment;
import com.ticonsys.prayerguide.util.AppSettings;
import com.ticonsys.prayerguide.util.ScreenUtils;
import com.ticonsys.prayerguide.widget.FragmentStatePagerAdapter;
import com.ticonsys.prayerguide.widget.SlidingTabLayout;

import org.jsoup.Jsoup;


public class SalaatTimesActivity extends AppCompatActivity implements Constants,
    InitialConfigFragment.OnOptionSelectedListener, ViewPager.OnPageChangeListener,
    LocationHelper.LocationCallback {

  private LocationHelper mLocationHelper;
  private Location mLastLocation = null;

  private ViewPager mPager;
  private ScreenSlidePagerAdapter mAdapter;
  private SlidingTabLayout mTabs;
  String currentVersion, latestVersion;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);


    AppSettings settings = AppSettings.getInstance(this);
    //INIT APP
    if (!settings.getBoolean(AppSettings.Key.IS_INIT)) {
      settings.set(settings.getKeyFor(AppSettings.Key.IS_ALARM_SET,         0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_FAJR_ALARM_SET,    0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_DHUHR_ALARM_SET,   0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_ASR_ALARM_SET,     0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_MAGHRIB_ALARM_SET, 0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_ISHA_ALARM_SET,    0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_SEHRI_ALARM_SET,   0), true);
      settings.set(settings.getKeyFor(AppSettings.Key.IS_IFTAR_ALARM_SET,  0), true);

      settings.set(AppSettings.Key.HAS_DEFAULT_SET, true);
      onUseDefaultSelected();

      settings.set(AppSettings.Key.USE_ADHAN, true);
      settings.set(AppSettings.Key.IS_INIT, true);
    }

    setContentView(R.layout.activity_salaat_times);

    ScreenUtils.lockOrientation(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
   // toolbar.setVisibility(View.GONE);

    mLocationHelper = (LocationHelper) getFragmentManager().findFragmentByTag(LOCATION_FRAGMENT);



    // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
    mAdapter = new ScreenSlidePagerAdapter(getFragmentManager(),0);

    mPager = (ViewPager) findViewById(R.id.pager);
    mPager.setAdapter(mAdapter);
    mPager.addOnPageChangeListener(this);

    mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
    mTabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
   // mTabs.setFitsSystemWindows(true);

    mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.white));

    // Setting the ViewPager For the SlidingTabsLayout
    mTabs.setViewPager(mPager);

    if(mLocationHelper == null) {
      mLocationHelper = LocationHelper.newInstance();
      getFragmentManager().beginTransaction().add(mLocationHelper, LOCATION_FRAGMENT).commit();
    }

    /*if (!settings.getBoolean(AppSettings.Key.IS_TNC_ACCEPTED, false)) {
      getWindow().getDecorView().postDelayed(new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(SalaatTimesActivity.this, TermsAndConditionsActivity.class);
          overridePendingTransition(R.anim.enter_from_bottom, R.anim.no_animation);
          startActivityForResult(intent, REQUEST_TNC);
        }
      }, 2000);
    }*/


    if(isNetworkConnected()){
      makeAppUpdateInfo();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mLastLocation == null) {
      fetchLocation();
    }
  }

  @Override
  protected void onDestroy() {
    //Just to be sure memory is cleaned up.
    mPager.removeOnPageChangeListener(this);
    mPager = null;
    mAdapter = null;
    mTabs = null;
    mLastLocation = null;

    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_salaat_times, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      startOnboardingFor(0);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void startOnboardingFor(int index) {
    Intent intent = new Intent(getApplicationContext(), OnboardingActivity.class);
    intent.putExtra(OnboardingActivity.EXTRA_CARD_INDEX, index);
    startActivityForResult(intent, REQUEST_ONBOARDING);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CHECK_SETTINGS) {
      switch (resultCode) {
        case Activity.RESULT_OK:
          // All required changes were successfully made
          fetchLocation();
          break;
        case Activity.RESULT_CANCELED:
          // The user was asked to change settings, but chose not to
          onLocationSettingsFailed();
          break;
        default:
          onLocationSettingsFailed();
          break;
      }
    } else if (requestCode == REQUEST_ONBOARDING) {
      if (resultCode == RESULT_OK) {
        onUseDefaultSelected();
      }
    } else if (requestCode == REQUEST_TNC) {
      if (resultCode == RESULT_CANCELED) {
        finish();
      } else {
        AppSettings settings = AppSettings.getInstance(this);
        settings.set(AppSettings.Key.IS_TNC_ACCEPTED, true);
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  private void fetchLocation() {
    if (mLocationHelper != null) {
      mLocationHelper.checkLocationPermissions();
    }
  }

  @Override
  public void onLocationSettingsFailed() {

  }

  @Override
  public void onLocationChanged(Location location) {
    mLastLocation = location;
    // NOT THE BEST SOLUTION, THINK OF SOMETHING ELSE
    mAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), 0);
    mPager.setAdapter(mAdapter);
  }

  @Override
  public void onConfigNowSelected(int num) {
    startOnboardingFor(num);
  }

  @Override
  public void onUseDefaultSelected() {
    if (mLastLocation != null) {
      // NOT THE BEST SOLUTION, THINK OF SOMETHING ELSE
      mAdapter = new ScreenSlidePagerAdapter(getFragmentManager(),0);
      mPager.setAdapter(mAdapter);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override
  public void onPageSelected(int position) {
    /*if (position == 0) {

    } else if (position == 1) {

      if (mAdapter.mKaabaLocatorFragment != null &&
          PermissionUtil.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        mAdapter.mKaabaLocatorFragment.showMap();
      }
    }  else {
      mAdapter.mKaabaLocatorFragment.hideMap();
    }*/
  }

  @Override
  public void onPageScrollStateChanged(int state) {

  }

  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private int mCardIndex;
   // public KaabaLocatorFragment mKaabaLocatorFragment;

    public ScreenSlidePagerAdapter(FragmentManager fm, int index) {
      super(fm);
      mCardIndex = index;
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          if (AppSettings.getInstance(getApplicationContext()).isDefaultSet()) {
            return SalaatTimesFragment.newInstance(mCardIndex, mLastLocation);
          } else {
            return InitialConfigFragment.newInstance();
          }

        case 1:
          return QiblaDirectionFragment.newInstance();

        case 2:
          return OfflineQuranFragment.newInstance();
      }
      return null;
    }

    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position == 0) {
        return getString(R.string.salaat_times);
      } else if (position == 1){
       // return getString(R.string.kaaba_position);
        return getString(R.string.qibla_direction);
      } else {
        return getString(R.string.quran_offline);
      }
    }
  }


  private boolean isNetworkConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null;
  }

  private void makeAppUpdateInfo() {
    PackageInfo pinfo = null;
    try {
      pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    int versionNumber = pinfo.versionCode;
    String versionName = pinfo.versionName;

    Log.d("NamazGuide", "version : " + versionNumber + " " + versionName);

    currentVersion = pinfo.versionName;

    new GetLatestVersion().execute();

  }

  private class GetLatestVersion extends AsyncTask<Void, String, String> {
    @Override
    protected String doInBackground(Void... voids) {

      String newVersion = null;
      try {
        newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=com.ticonsys.namazguide" + "" + "&hl=it")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get()
                .select("div[itemprop=softwareVersion]")
                .first()
                .ownText();
        return newVersion;
      } catch (Exception e) {
        return newVersion;
      }
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
      super.onPostExecute(onlineVersion);
      if (onlineVersion != null && !onlineVersion.isEmpty()) {
        if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
          //show dialog
          showUpdateDialog();
        }
      }
      Log.d("AppVersion", "Current version " + currentVersion + " playstore version " + onlineVersion);
    }

  }

  private void showUpdateDialog(){
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("A New Version is Available!");
    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ticonsys.namazguide")));
        dialog.dismiss();
      }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // background.start();
        dialog.dismiss();
      }
    });

    builder.setCancelable(false);
    builder.show();
  }

}
