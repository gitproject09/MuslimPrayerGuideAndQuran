package com.ticonsys.prayerguide.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ticonsys.prayerguide.R;
import com.ticonsys.prayerguide.util.Compass;

public class QiblaDirectionFragment extends Fragment {

  private static final String TAG = "CompassActivity";

  private Compass compass;

  public static QiblaDirectionFragment newInstance() {
    QiblaDirectionFragment fragment = new QiblaDirectionFragment();
    return fragment;
  }

  public QiblaDirectionFragment() {
    // Required empty public constructor
  }



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.activity_compass, container, false);

    compass = new Compass(getActivity());
    compass.arrowView = (ImageView) view.findViewById(R.id.main_image_hands);

    return view;
  }

 /* @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
  }*/


  @Override
  public void onStart() {
    super.onStart();
    Log.d(TAG, "start compass");
    compass.start();
  }

  @Override
  public void onPause() {
    super.onPause();
    compass.stop();
  }

  @Override
  public void onResume() {
    super.onResume();
    compass.start();
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d(TAG, "stop compass");
    compass.stop();
  }


}
