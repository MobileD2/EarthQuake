package com.mobiled2.earthquake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ToolbarActivity {
  private static final String TAG = "EARTHQUAKE_ACTIVITY";

  private static final int MENU_PREFERENCES = Menu.FIRST + 1;
  private static final int MENU_UPDATE = Menu.FIRST + 2;

  private static final int SHOW_PREFERENCES = 1;

  private ViewPager viewPager;

  public int minimumMagnitude = 0;
  public boolean autoUpdateChecked = false;
  public int updateFreq = 0;
  public int actionBarIndex = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    updateFromPreferences();
    initTabLayout();
  }

  @Override
  public void onStop() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

    editor.putString(PreferencesActivity.PREF_ACTION_BAR_INDEX, String.valueOf(viewPager.getCurrentItem()));
    editor.apply();

    super.onStop();
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
      startService(new Intent(this, UpdateService.class));
    }
  }

  private void updateFromPreferences() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
    minimumMagnitude = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "0"));
    updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "-1"));
    actionBarIndex = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_ACTION_BAR_INDEX, "0"));
  }

  private void initTabLayout() {
    TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);

    tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_list));
    tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_map));
    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

    final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

    viewPager = (ViewPager)findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    viewPager.setCurrentItem(actionBarIndex);

    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {

      }
    });
  }
}
