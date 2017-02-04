package com.mobiled2.earthquake;

import android.content.Intent;
import android.os.Bundle;

public class SearchActivity extends ToolbarActivity {
  private static final String TAG = "SEARCH_ACTIVITY";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    restartLoader(intent);
  }

  private void restartLoader(Intent intent) {
    SearchFragment fragment = (SearchFragment)getSupportFragmentManager().findFragmentById(R.id.SearchFragment);
    fragment.restartLoader(intent);
  }
}
