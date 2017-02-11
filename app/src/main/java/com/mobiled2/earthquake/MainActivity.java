package com.mobiled2.earthquake;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ToolbarActivity {
  private static final String TAG = "EARTHQUAKE_ACTIVITY";

  private static final int MENU_PREFERENCES = Menu.FIRST + 1;
  private static final int MENU_UPDATE = Menu.FIRST + 2;

  private static final int SHOW_PREFERENCES = 1;

  private SharedPreferences prefs;
  private ViewPager viewPager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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

    getMenuInflater().inflate(R.menu.main_menu, menu);
    initSearchView(menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch(item.getItemId()) {
      case (R.id.menu_preferences): {
        startActivityForResult(new Intent(this, PreferencesActivity.class), SHOW_PREFERENCES);
        return true;
      }
      case (R.id.menu_refresh): {
        startService(new Intent(this, UpdateService.class));
        return true;
      }
      default: return false;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == SHOW_PREFERENCES) {
      startService(new Intent(this, UpdateService.class));
    }
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
    viewPager.setCurrentItem(Integer.parseInt(prefs.getString(PreferencesActivity.PREF_ACTION_BAR_INDEX, "0")));

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

  private void initSearchView(Menu menu) {
    SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
  }
}
