package com.mobiled2.earthquake;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
  public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
  public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
  public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

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
