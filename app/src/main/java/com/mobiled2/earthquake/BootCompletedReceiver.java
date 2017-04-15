package com.mobiled2.earthquake;

import android.preference.PreferenceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)) {
      context.startService(new Intent(context, UpdateService.class));
    }
  }
}
