package com.ticonsys.prayerguide.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ticonsys.prayerguide.R;
import com.ticonsys.prayerguide.ShowSurahDetailsActivity;

public class OfflineQuranFragment extends Fragment {

  String[] all_surah_list;
  public static OfflineQuranFragment newInstance() {
    OfflineQuranFragment fragment = new OfflineQuranFragment();
    return fragment;
  }

  public OfflineQuranFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_offline_quran, container, false);
    all_surah_list = getResources().getStringArray(R.array.all_surah_list);


    ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_row, all_surah_list);
    ListView mlistView = (ListView) view.findViewById(R.id.list);
    mlistView.setAdapter(adapter);
    mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // When clicked, show a toast with the TextView text
       // Toast.makeText(getActivity(), ((TextView) view).getText() +" Pos: "+ position, Toast.LENGTH_SHORT).show();
        int surah_number = position + 1;
        String surah_name = ((TextView) view).getText().toString();

        Intent intent = new Intent(getActivity(), ShowSurahDetailsActivity.class);
        intent.putExtra("surah_number", ""+surah_number);
        intent.putExtra("surah_name", surah_name);
        getActivity().startActivity(intent);
      }
    });

    return view;
  }


}
