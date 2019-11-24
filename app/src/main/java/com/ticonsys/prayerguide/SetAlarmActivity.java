package com.ticonsys.prayerguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ticonsys.prayerguide.util.AlarmUtils;
import com.ticonsys.prayerguide.util.AppSettings;
import com.ticonsys.prayerguide.util.ScreenUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetAlarmActivity extends AppCompatActivity implements Constants,
    CompoundButton.OnCheckedChangeListener, View.OnClickListener {

  int mIndex = 0;
  AppSettings settings;
  CheckBox mAlarm;
  CheckBox mAscending;
  CheckBox mRandom;
  CheckBox mAdhan;
  TextView mRingtone;
  TextView[] mPrayers = new TextView[5];

  SetAlarmRamadanHelper mRamadanHelper;
  Map<String, Uri> mRingtonesMap;
  private Uri mLastSelectedRingtone = null;
  private String mLastSelectedRingtoneName = "Default";
  private MediaPlayer mMediaPlayer;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ScreenUtils.lockOrientation(this);
    setContentView(R.layout.activity_set_alarm);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mAlarm = (CheckBox) findViewById(R.id.alarm);
    mAscending = (CheckBox) findViewById(R.id.ascending_alarm);
    mRandom = (CheckBox) findViewById(R.id.random_ringtone);
    mAdhan = (CheckBox) findViewById(R.id.adhan_ringtone);
    mRingtone = (TextView) findViewById(R.id.ringtone);

    mPrayers[0] = (TextView) findViewById(R.id.fajr);
    mPrayers[1] = (TextView) findViewById(R.id.dhuhr);
    mPrayers[2] = (TextView) findViewById(R.id.asr);
    mPrayers[3] = (TextView) findViewById(R.id.maghrib);
    mPrayers[4] = (TextView) findViewById(R.id.isha);

    /*mPrayers[5] = (TextView) findViewById(R.id.sehri);
    mPrayers[6] = (TextView) findViewById(R.id.iftar);*/

    for (int i = 0; i < mPrayers.length; i++) {
      TextView tv = mPrayers[i];
      tv.setOnClickListener(this);
      tv.setTag(i);
    }

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(EXTRA_ALARM_INDEX)) {
      mIndex = intent.getIntExtra(EXTRA_ALARM_INDEX, 0);
      Log.d("ValueOfmIndex", ""+mIndex);
    }

    settings = AppSettings.getInstance(this);

    // Init the alarm settings
    if (settings.isAlarmSetFor(mIndex)) {
      mAlarm.setChecked(true);
      for (int i = 0; i < mPrayers.length; i++) {
        TextView tv = mPrayers[i];
        tv.setEnabled(true);
        boolean status = getPrayerAlarmStatus(i);
        tv.setSelected(status);
      }
    }

    mAscending.setChecked(settings.getBoolean(AppSettings.Key.IS_ASCENDING_ALARM));
    mRandom.setChecked(settings.getBoolean(AppSettings.Key.IS_RANDOM_ALARM));
    mAdhan.setChecked(settings.getBoolean(AppSettings.Key.USE_ADHAN));

    //attach listener after init to prevent unpredictable result.
    mAlarm.setOnCheckedChangeListener(this);
    mAscending.setOnCheckedChangeListener(this);
    mRandom.setOnCheckedChangeListener(this);
    mAdhan.setOnCheckedChangeListener(this);

    mRamadanHelper = new SetAlarmRamadanHelper(this, mIndex);
    setupRingtoneSelection();
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int id = buttonView.getId();
    if (id == mAlarm.getId()) {
      settings.setAlarmFor(mIndex, isChecked);
      for (int i = 0; i < mPrayers.length; i++) {
        TextView tv = mPrayers[i];
        tv.setEnabled(isChecked);
        tv.setSelected(isChecked);
        setPrayerAlarmStatus(i, isChecked);
      }

      if (!isChecked) {
        mRamadanHelper.setRamadanOption(false);
      }
      mRamadanHelper.setRamadanOptionEnabled(isChecked);

    } else if (id == mAscending.getId()) {
      settings.set(AppSettings.Key.IS_ASCENDING_ALARM, isChecked);

    } else if (id == mRandom.getId()) {
      settings.set(AppSettings.Key.IS_RANDOM_ALARM, isChecked);
      if (isChecked) {
        mAdhan.setChecked(false);
      }

    } else if (id == mAdhan.getId()) {
      settings.set(AppSettings.Key.USE_ADHAN, isChecked);
      if (isChecked) {
        mRandom.setChecked(false);
      }
    }
  }

  @Override
  public void onClick(View v) {
    int index = (Integer) v.getTag();

    Log.d("positionSalat", ""+index);
    boolean isSet = getPrayerAlarmStatus(index);
    setPrayerAlarmStatus(index, !isSet);
    v.setSelected(!isSet);
  }

  private void setPrayerAlarmStatus(int prayerIndex, boolean isOn) {
    String key = getPrayerKeyFromIndex(prayerIndex);

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
    }
    return key;
  }

  @Override
  public void onBackPressed() {
    boolean anyAlarmSelected = false;
    for (TextView tv : mPrayers) {
      if (tv.isSelected()) {
        anyAlarmSelected = true;
      }
    }

    if (!anyAlarmSelected) {
      mAlarm.setChecked(false);
    }

    Intent data = new Intent();
    if (getParent() == null) {
      setResult(RESULT_OK, data);
    } else {
      getParent().setResult(RESULT_OK, data);
    }

    super.onBackPressed();
  }

  private void setupRingtoneSelection() {
    mRingtonesMap = AlarmUtils.getRingtones(SetAlarmActivity.this);

    mRingtone.setText(getString(R.string.set_alarm_select_ringtone,
        settings.getString(AppSettings.Key.SELECTED_RINGTONE_NAME, mLastSelectedRingtoneName)));

    mRingtone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final List<String> ringtoneKeys = new ArrayList<>(mRingtonesMap.keySet());
        String ringtoneUri = settings.getString(AppSettings.Key.SELECTED_RINGTONE, "");
        int selected = -1;
        for (int i = 0; i < ringtoneKeys.size(); i++) {
          String ring = ringtoneKeys.get(i);
          if (mRingtonesMap.get(ring).toString().equalsIgnoreCase(ringtoneUri)) {
            selected = i;
            break;
          }
        }

        AlarmUtils.getRingtonesDialog(SetAlarmActivity.this, ringtoneKeys, selected, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) { //item selected
            String name = ringtoneKeys.get(which);
            mLastSelectedRingtoneName = name;
            mLastSelectedRingtone = mRingtonesMap.get(name);

            try {
              if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
              } else {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
              }
              mMediaPlayer.setDataSource(SetAlarmActivity.this, mLastSelectedRingtone);
              mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
              mMediaPlayer.setLooping(false);
              mMediaPlayer.prepare();
              mMediaPlayer.start();
            } catch (IOException ioe) {
              //do nothing
            }

          }
        }, new DialogInterface.OnClickListener() { //ok
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (mMediaPlayer != null) {
              mMediaPlayer.stop();
              mMediaPlayer.release();
              mMediaPlayer = null;
            }
            settings.set(AppSettings.Key.SELECTED_RINGTONE, mLastSelectedRingtone.toString());
            settings.set(AppSettings.Key.SELECTED_RINGTONE_NAME, mLastSelectedRingtoneName);
            //Update the textview
            mRingtone.setText(getString(R.string.set_alarm_select_ringtone,
                settings.getString(AppSettings.Key.SELECTED_RINGTONE_NAME, mLastSelectedRingtoneName)));
            dialog.dismiss();
          }
        }, new DialogInterface.OnClickListener() { //cancel
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (mMediaPlayer != null) {
              mMediaPlayer.stop();
              mMediaPlayer.release();
              mMediaPlayer = null;
            }
            dialog.dismiss();
          }
        });
      }
    });
  }

  @Override
  protected void onDestroy() {
    if (mMediaPlayer != null) {
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.stop();
      }

      mMediaPlayer.release();
      mMediaPlayer = null;
    }

    mRingtone.setOnClickListener(null);

    super.onDestroy();
  }
}
