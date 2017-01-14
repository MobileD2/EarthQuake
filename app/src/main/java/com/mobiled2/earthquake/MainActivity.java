package com.mobiled2.earthquake;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
  private static final int MENU_PREFERENCES = Menu.FIRST + 1;
  private static final int MENU_UPDATE = Menu.FIRST + 2;

  private static final int SHOW_PREFERENCES = 1;

  public int minimumMagnitude = 0;
  public boolean autoUpdateChecked = false;
  public int updateFreq = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    updateFromPreferences();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch(item.getItemId()) {
      case (MENU_PREFERENCES): {
        startActivityForResult(new Intent(this, PreferencesActivity.class), SHOW_PREFERENCES);

        return true;
      }
    }

    return false;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == SHOW_PREFERENCES) {
      updateFromPreferences();

      FragmentManager fm = getSupportFragmentManager();
      final EarthquakeListFragment earthquakeListFragment = (EarthquakeListFragment)fm.findFragmentById(R.id.EarthquakeListFragment);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          earthquakeListFragment.refreshEarthquakes();
        }
      });

      t.start();
    }
  }

  private void updateFromPreferences() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
    minimumMagnitude = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "0"));
    updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "-1"));
  }
}
