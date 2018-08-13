package com.ticonsys.prayerguide.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ticonsys.prayerguide.Constants;
import com.ticonsys.prayerguide.R;
import com.ticonsys.prayerguide.SetAlarmActivity;
import com.ticonsys.prayerguide.scheduler.RamadanAlarmReceiver;
import com.ticonsys.prayerguide.scheduler.SalaatAlarmReceiver;
import com.ticonsys.prayerguide.util.AppSettings;
import com.ticonsys.prayerguide.util.PrayTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class SalaatTimesFragment extends Fragment implements Constants, View.OnClickListener {
  private static boolean sIsAlarmInit = false;
  int mIndex = 0;
  Location mLastLocation;
  TextView mAlarm;
  View mRamadanContainer;

  AppSettings settings;

  TextView[] mPrayers = new TextView[7];
  RelativeLayout rlFajr, rlZuhr, rlAsr, rlMaghrib, rlIsha, rlSehri, rlIftar;

  ImageView fajrBtn, zuhrBtn, asrBtn, maghribBtn, ishaBtn, sehriBtn, iftarBtn;

  public static SalaatTimesFragment newInstance(int index, Location location) {
    SalaatTimesFragment fragment = new SalaatTimesFragment();
    Bundle args = new Bundle();
    args.putInt(EXTRA_ALARM_INDEX, index);
    args.putParcelable(EXTRA_LAST_LOCATION, location);
    fragment.setArguments(args);
    return fragment;
  }

  public SalaatTimesFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mIndex = getArguments().getInt(EXTRA_ALARM_INDEX);
      mLastLocation = (Location) getArguments().getParcelable(EXTRA_LAST_LOCATION);
    }

    settings = AppSettings.getInstance(getActivity());

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_salaat_times, container, false);
    init(view);
    return view;
  }

  protected void init(View view) {
    // In future releases we will add more cards.
    // Then we'll need to do this for each card.
    // For now it's included in the layout which
    // makes it easier to work with the layout editor.
    // inflater.inflate(R.layout.view_prayer_times, timesContainer, true);

    if (mLastLocation == null) {
      return;
    }


    //Toolbar will now take on default Action Bar characteristics
    LinkedHashMap<String, String> prayerTimes =
        PrayTime.getPrayerTimes(getActivity(), mIndex, mLastLocation.getLatitude(), mLastLocation.getLongitude());
          //  PrayTime.getPrayerTimes(getActivity(), mIndex, 37.517235, 127.047325);

    TextView title = (TextView) view.findViewById(R.id.card_title);
    TextView today = (TextView) view.findViewById(R.id.date);
    title.setText(TimeZone.getDefault().getID());

    DateFormat df = new SimpleDateFormat("EEE, d MMM, yyyy");

    String date = df.format(Calendar.getInstance().getTime());
    today.setText(date);

    TextView fajr = (TextView) view.findViewById(R.id.fajr);
    TextView dhuhr = (TextView) view.findViewById(R.id.dhuhr);
    TextView asr = (TextView) view.findViewById(R.id.asr);
    TextView maghrib = (TextView) view.findViewById(R.id.maghrib);
    TextView isha = (TextView) view.findViewById(R.id.isha);

    TextView sehri = (TextView) view.findViewById(R.id.sehri);
    TextView iftar = (TextView) view.findViewById(R.id.iftar);

    TextView sunrise = (TextView) view.findViewById(R.id.sunrise);
    TextView sunset = (TextView) view.findViewById(R.id.sunset);


    mPrayers[0] = (TextView) view.findViewById(R.id.fajr);
    mPrayers[1] = (TextView) view.findViewById(R.id.dhuhr);
    mPrayers[2] = (TextView) view.findViewById(R.id.asr);
    mPrayers[3] = (TextView) view.findViewById(R.id.maghrib);
    mPrayers[4] = (TextView) view.findViewById(R.id.isha);

    mPrayers[5] = (TextView) view.findViewById(R.id.sehri);
    mPrayers[6] = (TextView) view.findViewById(R.id.iftar);


    rlFajr = (RelativeLayout) view.findViewById(R.id.rlFajr);
    rlZuhr = (RelativeLayout) view.findViewById(R.id.rlZuhr);
    rlAsr = (RelativeLayout) view.findViewById(R.id.rlAsr);
    rlMaghrib = (RelativeLayout) view.findViewById(R.id.rlMaghrib);
    rlIsha = (RelativeLayout) view.findViewById(R.id.rlIsha);

    rlSehri = (RelativeLayout) view.findViewById(R.id.rlSehri);
    rlIftar = (RelativeLayout) view.findViewById(R.id.rlIftar);

    fajrBtn = (ImageView) view.findViewById(R.id.fajrBtn);
    zuhrBtn = (ImageView) view.findViewById(R.id.zuhrBtn);
    asrBtn = (ImageView) view.findViewById(R.id.asrBtn);
    maghribBtn = (ImageView) view.findViewById(R.id.maghribBtn);
    ishaBtn = (ImageView) view.findViewById(R.id.ishaBtn);

    sehriBtn = (ImageView) view.findViewById(R.id.sehriBtn);
    iftarBtn = (ImageView) view.findViewById(R.id.iftarBtn);

    rlFajr.setOnClickListener(this);
    rlZuhr.setOnClickListener(this);
    rlAsr.setOnClickListener(this);
    rlMaghrib.setOnClickListener(this);
    rlIsha.setOnClickListener(this);

    rlSehri.setOnClickListener(this);
    rlIftar.setOnClickListener(this);

    if (settings.isAlarmSetFor(mIndex)) {

      for (int i = 0; i < mPrayers.length; i++) {
        TextView tv = mPrayers[i];
        tv.setEnabled(true);
        boolean status = getPrayerAlarmStatus(i);
        if(status){
          Log.d("AlarmStatus", "True");

        } else{
          Log.d("AlarmStatus", "False");
        }
        if(i == 0){
          if(status){
            fajrBtn.setImageResource(R.drawable.alarm_on);
          }else{
            fajrBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 1){
          if(status){
            zuhrBtn.setImageResource(R.drawable.alarm_on);
          }else{
            zuhrBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 2){
          if(status){
            asrBtn.setImageResource(R.drawable.alarm_on);
          }else{
            asrBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 3){
          if(status){
            maghribBtn.setImageResource(R.drawable.alarm_on);
          }else{
            maghribBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 4){
          if(status){
            ishaBtn.setImageResource(R.drawable.alarm_on);
          }else{
            ishaBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 5){
          if(status){
            sehriBtn.setImageResource(R.drawable.alarm_on);
          }else{
            sehriBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        if(i == 6){
          if(status){
            iftarBtn.setImageResource(R.drawable.alarm_on);
          }else{
            iftarBtn.setImageResource(R.drawable.alarm_off);
          }
        }

        tv.setSelected(status);
      }
    }


    mAlarm = (TextView) view.findViewById(R.id.alarm);
    mAlarm.setVisibility(View.GONE);
    mRamadanContainer = view.findViewById(R.id.ramadan_container);

    fajr.setText(prayerTimes.get(String.valueOf(fajr.getTag())));
    dhuhr.setText(prayerTimes.get(String.valueOf(dhuhr.getTag())));
    asr.setText(prayerTimes.get(String.valueOf(asr.getTag())));
    maghrib.setText(prayerTimes.get(String.valueOf(maghrib.getTag())));
    isha.setText(prayerTimes.get(String.valueOf(isha.getTag())));
    sunrise.setText(prayerTimes.get(String.valueOf(sunrise.getTag())));
    sunset.setText(prayerTimes.get(String.valueOf(sunset.getTag())));

    Log.d("sehriIftar", ""+prayerTimes.get(String.valueOf(sehri.getTag())) + " "+prayerTimes.get(String.valueOf(iftar.getTag())));

    sehri.setText(prayerTimes.get(String.valueOf(sehri.getTag())));
    iftar.setText(prayerTimes.get(String.valueOf(maghrib.getTag())));

    //set text for the first card.
    setAlarmButtonText(mAlarm, mIndex);
    setAlarmButtonClickListener(mAlarm, mIndex);

    if (!sIsAlarmInit) {
      if (AppSettings.getInstance().isDefaultSet()) {
        AppSettings.getInstance().setLatFor(mIndex, mLastLocation.getLatitude());
        AppSettings.getInstance().setLngFor(mIndex, mLastLocation.getLongitude());
        updateAlarmStatus();
        sIsAlarmInit = true;
      }
    }
  }

  private void setAlarmButtonText(TextView button, int index) {
    boolean isAlarmSet = AppSettings.getInstance(getActivity()).isAlarmSetFor(index);
    int isAlarmSetInt = isAlarmSet ? 0 : 1;
    String buttonText = getResources().getQuantityString(R.plurals.button_alarm, isAlarmSetInt);
    button.setText(buttonText);
    boolean isRamadanSet = AppSettings.getInstance(getActivity()).getBoolean(AppSettings.Key.IS_RAMADAN);
    mRamadanContainer.setVisibility(isRamadanSet? View.VISIBLE : View.GONE);
  }

  private void setAlarmButtonClickListener(TextView alarm, int index) {
    alarm.setOnClickListener(new View.OnClickListener() {
      int index = 0;

      @Override
      public void onClick(View v) {
        AppSettings settings = AppSettings.getInstance(getActivity());
        settings.setLatFor(mIndex, mLastLocation.getLatitude());
        settings.setLngFor(mIndex, mLastLocation.getLongitude());
        Intent intent = new Intent(getActivity(), SetAlarmActivity.class);
        intent.putExtra(EXTRA_ALARM_INDEX, index);
        startActivityForResult(intent, REQUEST_SET_ALARM);
      }

      public View.OnClickListener init(int index) {
        this.index = index;
        return this;
      }

    }.init(index));
  }

  public void setLocation(Location location) {
    mLastLocation = location;
    AppSettings.getInstance().setLatFor(mIndex, location.getLatitude());
    AppSettings.getInstance().setLngFor(mIndex, location.getLatitude());
    if (isAdded()) {
      init(getView());
    }
  }

  private void updateAlarmStatus() {
    setAlarmButtonText(mAlarm, mIndex);

    AppSettings settings = AppSettings.getInstance(getActivity());

    SalaatAlarmReceiver sar = new SalaatAlarmReceiver();
    boolean isAlarmSet = settings.isAlarmSetFor(mIndex);
    sar.cancelAlarm(getActivity());
    if (isAlarmSet) {
      sar.setAlarm(getActivity());
    }

    RamadanAlarmReceiver rar = new RamadanAlarmReceiver();
    boolean isRamadanAlarmSet = settings.getBoolean(AppSettings.Key.IS_RAMADAN);
    rar.cancelAlarm(getActivity());
    if (isRamadanAlarmSet) {
      rar.setAlarm(getActivity());
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_SET_ALARM) {
      if (resultCode == Activity.RESULT_OK) {
        updateAlarmStatus();
      } else {
        super.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  @Override
  public void onClick(View view) {

    boolean isSet;
    switch (view.getId()) {

      case R.id.rlFajr:

        isSet = getPrayerAlarmStatus(0);
        setPrayerAlarmStatus(0, !isSet);
        view.setSelected(!isSet);

        if(isSet){
          fajrBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Fajr Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          fajrBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Fajr Alarm turned on", Toast.LENGTH_LONG).show();
        }

        Log.d("NamazSet", "Fajr "+isSet);

        break;

      case R.id.rlZuhr:

        isSet = getPrayerAlarmStatus(1);
        setPrayerAlarmStatus(1, !isSet);
        view.setSelected(!isSet);

        if(isSet){
          zuhrBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Dhuhur Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          zuhrBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Dhuhur Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Zuhr "+isSet);
        break;

      case R.id.rlAsr:

        isSet = getPrayerAlarmStatus(2);
        setPrayerAlarmStatus(2, !isSet);
        view.setSelected(!isSet);
        if(isSet){
          asrBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Asr Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          asrBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Asr Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Asr "+isSet);
        break;

      case R.id.rlMaghrib:

        isSet = getPrayerAlarmStatus(3);
        setPrayerAlarmStatus(3, !isSet);
        view.setSelected(!isSet);
        if(isSet){
          maghribBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Maghrib Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          maghribBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Maghrib Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Maghrib "+isSet);
        break;

      case R.id.rlIsha:

        isSet = getPrayerAlarmStatus(4);
        setPrayerAlarmStatus(4, !isSet);
        view.setSelected(!isSet);
        if(isSet){
          ishaBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Isha Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          ishaBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Isha Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Isha "+isSet);
        break;

      case R.id.rlSehri:

        isSet = getPrayerAlarmStatus(5);
        setPrayerAlarmStatus(5, !isSet);
        view.setSelected(!isSet);
        if(isSet){
          sehriBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Sehri Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          sehriBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Sehri Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Sehri "+isSet);
        break;

      case R.id.rlIftar:

        isSet = getPrayerAlarmStatus(6);
        setPrayerAlarmStatus(6, !isSet);
        view.setSelected(!isSet);
        if(isSet){
          iftarBtn.setImageResource(R.drawable.alarm_off);
          Toast.makeText(getActivity(), "Iftar Alarm turned off", Toast.LENGTH_LONG).show();
        } else{
          iftarBtn.setImageResource(R.drawable.alarm_on);
          Toast.makeText(getActivity(), "Iftar Alarm turned on", Toast.LENGTH_LONG).show();
        }
        Log.d("NamazSet", "Iftar "+isSet);
        break;


    }

  }

  private void setPrayerAlarmStatus(int prayerIndex, boolean isOn) {
    String key = getPrayerKeyFromIndex(prayerIndex);

    Log.d("namazKey", ""+key);

    if (key != null) {
      settings.set(key, isOn);
    }
  }

  private boolean getPrayerAlarmStatus(int prayerIndex) {
    String key = getPrayerKeyFromIndex(prayerIndex);

    if (key != null) {
      return settings.getBoolean(key);
    }
    return false;
  }

  private String getPrayerKeyFromIndex(int prayerIndex) {
    String key = null;
    switch (prayerIndex) {
      case 0:
        key = settings.getKeyFor(AppSettings.Key.IS_FAJR_ALARM_SET, mIndex);
        break;
      case 1:
        key = settings.getKeyFor(AppSettings.Key.IS_DHUHR_ALARM_SET, mIndex);
        break;
      case 2:
        key = settings.getKeyFor(AppSettings.Key.IS_ASR_ALARM_SET, mIndex);
        break;
      case 3:
        key = settings.getKeyFor(AppSettings.Key.IS_MAGHRIB_ALARM_SET, mIndex);
        break;
      case 4:
        key = settings.getKeyFor(AppSettings.Key.IS_ISHA_ALARM_SET, mIndex);
        break;

      case 5:
        key = settings.getKeyFor(AppSettings.Key.IS_SEHRI_ALARM_SET, mIndex);
        break;

      case 6:
        key = settings.getKeyFor(AppSettings.Key.IS_IFTAR_ALARM_SET, mIndex);
        break;
    }
    return key;
  }


}
