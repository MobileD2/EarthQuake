package com.mobiled2.earthquake;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

class PagerAdapter extends FragmentStatePagerAdapter {
  static final int LIST_POSITION = 0;
  static final int MAP_POSITION = 1;

  private int pageCount;

  PagerAdapter(FragmentManager fm, int pageCount) {
    super(fm);
    this.pageCount = pageCount;
  }

  @Override
  public Fragment getItem(int position) {
    switch(position) {
      case LIST_POSITION: return new ListFragment();
      case MAP_POSITION: return new MapFragment();
      default: return null;
    }
  }

  @Override
  public int getCount() {
    return pageCount;
  }
}
