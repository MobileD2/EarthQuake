package com.mobiled2.earthquake;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

class PagerAdapter extends FragmentStatePagerAdapter {
  private int pageCount;

  PagerAdapter(FragmentManager fm, int pageCount) {
    super(fm);
    this.pageCount = pageCount;
  }

  @Override
  public Fragment getItem(int position) {
    switch(position) {
      case 0: return new ListFragment();
      case 1: return new MapFragment();
      default: return null;
    }
  }

  @Override
  public int getCount() {
    return pageCount;
  }
}
