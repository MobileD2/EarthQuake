package com.mobiled2.earthquake;

import android.content.Intent;

interface IFragmentCallback {
  void onFragmentReady();
  int getFragmentPageAdapterPosition();
  boolean onFragmentShouldClick(Intent intent);
  void onFragmentClick(Intent intent);
}
