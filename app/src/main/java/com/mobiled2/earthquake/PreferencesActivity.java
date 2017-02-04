package com.mobiled2.earthquake;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class PreferencesActivity extends PreferenceActivity {
  public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
  public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
  public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
  public static final String PREF_ACTION_BAR_INDEX = "ACTION_BAR_INDEX";

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
