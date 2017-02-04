package com.mobiled2.earthquake;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ToolbarActivity extends AppCompatActivity {
  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
  }
}
