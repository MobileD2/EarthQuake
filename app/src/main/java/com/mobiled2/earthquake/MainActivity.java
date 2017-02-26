package com.mobiled2.earthquake;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends ToolbarActivity implements IFragmentCallback {
  private static final String TAG = "EARTHQUAKE_ACTIVITY";

  private static final String STATE_IN_PERMISSION = "STATE_IN_PERMISSION";

  public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1337;

  private static final int MENU_PREFERENCES = Menu.FIRST + 1;
  private static final int MENU_UPDATE = Menu.FIRST + 2;

  private static final int SHOW_PREFERENCES = 1;

  private SharedPreferences prefs;

  private ViewPager viewPager;

  private boolean isInPermission = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      isInPermission = savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
    }

    onCreatePermitted(canGetLocation());
  }

  private boolean canGetLocation() {
    return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private void onCreatePermitted(boolean canGetLocation) {
    if (canGetLocation) {
      if (initGoogleApi()) {
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initTabLayout();
      }
    } else if (!isInPermission) {
      isInPermission = true;
      ActivityCompat.requestPermissions(this, new String[] {ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
  }

  @Override
  public void onStop() {
    if (!isInPermission) {
      SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

      editor.putString(PreferencesActivity.PREF_ACTION_BAR_INDEX, String.valueOf(viewPager.getCurrentItem()));
      editor.apply();
    }

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
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_PERMISSION_ACCESS_FINE_LOCATION: {
        if (canGetLocation()) {
          onCreatePermitted(true);
        } else {
          finish();
        }
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch(requestCode) {
      case (SHOW_PREFERENCES):
        startService(new Intent(this, UpdateService.class));
      break;
      default: break;
    }
  }

  protected boolean initGoogleApi() {
    GoogleApiAvailability checker = GoogleApiAvailability.getInstance();
    int status = checker.isGooglePlayServicesAvailable(this);

    if (status == ConnectionResult.SUCCESS) {
      if (getVersionFromPackageManager(this) >= 2) {
        return true;
      } else {
        Toast.makeText(this, R.string.google_maps_v2_not_available, Toast.LENGTH_LONG).show();
        finish();
      }
    }
    else if (checker.isUserResolvableError(status)) {
      Toast.makeText(this, R.string.google_play_services_out_of_date, Toast.LENGTH_LONG).show();
      finish();
    }
    else {
      Toast.makeText(this, R.string.google_maps_v2_not_available, Toast.LENGTH_LONG).show();
      finish();
    }

    return false;
  }

  private static int getVersionFromPackageManager(Context context) {
    PackageManager packageManager = context.getPackageManager();
    FeatureInfo[] featureInfos = packageManager.getSystemAvailableFeatures();

    if (featureInfos != null && featureInfos.length > 0) {
      for (FeatureInfo featureInfo : featureInfos) {
        if (featureInfo.name == null) {
          if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
            return (featureInfo.reqGlEsVersion & 0xffff0000) >> 16;
          }
          else {
            return 1;
          }
        }
      }
    }

    return 1;
  }

  private void initTabLayout() {
    TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);

    tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_list));
    tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_map));
    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

    PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

    viewPager = (ViewPager)findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    viewPager.setCurrentItem(Integer.parseInt(prefs.getString(PreferencesActivity.PREF_ACTION_BAR_INDEX, String.valueOf(PagerAdapter.LIST_POSITION))));

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

  @Override
  public void onFragmentClick(Intent intent) {
    viewPager.setCurrentItem(PagerAdapter.MAP_POSITION);

    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
      ((IFragmentCallback)fragment).onFragmentClick(intent);
    }
  }
}
