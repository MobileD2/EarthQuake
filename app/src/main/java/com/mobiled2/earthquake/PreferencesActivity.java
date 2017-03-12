package com.mobiled2.earthquake;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class PreferencesActivity extends PreferenceActivity {
  public static final String PREF_DATA_SOURCE = "PREF_DATA_SOURCE";
  public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
  public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
  public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
  public static final String PREF_RECORDS_COUNT = "PREF_RECORDS_COUNT";
  public static final String PREF_CLUSTERING_COLOR = "PREF_CLUSTERING_COLOR";
  public static final String PREF_ZOOM_WHEN_ITEM_CLICKED = "PREF_ZOOM_WHEN_ITEM_CLICKED";
  public static final String PREF_ACTION_BAR_INDEX = "PREF_ACTION_BAR_INDEX";
  public static final String PREF_NOTIFICATION = "PREF_NOTIFICATION";

  public static final int PREF_ALL_MAGNITUDE_VALUE = -1;

  public static final int PREF_TODAY_RECORDS_VALUE = -1;
  public static final int PREF_LAST_WEEK_RECORDS_VALUE = -2;
  public static final int PREF_LAST_MONTH_RECORDS_VALUE = -3;
  public static final int PREF_LAST_QUARTER_RECORDS_VALUE = -4;
  public static final int PREF_LAST_HALF_YEAR_RECORDS_VALUE = -5;
  public static final int PREF_LAST_YEAR_RECORDS_VALUE = -6;

  public static final int PREF_MAP_MAX_ZOOM_VALUE = -1;
  public static final int PREF_MAP_AUTO_ZOOM_VALUE = -2;

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      onCreatePreferencesActivity();
    } else {
      onCreatePreferencesFragment();
    }
  }

  @SuppressWarnings("deprecation")
  private void onCreatePreferencesActivity() {
    addPreferencesFromResource(R.xml.preferences);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void onCreatePreferencesFragment() {
    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new PreferencesFragment())
      .commit();
  }
}
