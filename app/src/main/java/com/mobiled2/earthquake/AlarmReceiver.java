package com.mobiled2.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
  public static final String ACTION_REFRESH_EARTHQUAKE_ALARM = "com.mobiled2.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService(new Intent(context, UpdateService.class));
  }
}
