package com.mobiled2.earthquake;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class QuakeDetailsActivity extends ToolbarActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quake_details);

    Intent intent = getIntent();

    ((TextView)findViewById(R.id.details_date)).setText(intent.getStringExtra(ContentProvider.KEY_DATE));
    ((TextView)findViewById(R.id.details_details)).setText(intent.getStringExtra(ContentProvider.KEY_DETAILS));
    ((TextView)findViewById(R.id.details_magnitude)).setText(intent.getStringExtra(ContentProvider.KEY_MAGNITUDE));
    ((TextView)findViewById(R.id.details_latitude)).setText(intent.getStringExtra(ContentProvider.KEY_LATITUDE));
    ((TextView)findViewById(R.id.details_longitude)).setText(intent.getStringExtra(ContentProvider.KEY_LONGITUDE));
    ((TextView)findViewById(R.id.details_depth)).setText(intent.getStringExtra(ContentProvider.KEY_DEPTH));
  }
}
