package com.mobiled2.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

class FragmentLoader {
  static Loader<Cursor> createLoader(Context context, SharedPreferences prefs) {
    float minimumMagnitude = Float.parseFloat(prefs.getString(PreferencesActivity.PREF_MIN_MAG, String.valueOf(PreferencesActivity.PREF_ALL_MAGNITUDE_VALUE)));
    int recordsCount = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_RECORDS_COUNT, String.valueOf(PreferencesActivity.PREF_TODAY_RECORDS_VALUE)));

    String[] projection = new String[] {
      ContentProvider.KEY_ID,
      ContentProvider.KEY_DATE,
      ContentProvider.KEY_MAGNITUDE,
      ContentProvider.KEY_DETAILS,
      ContentProvider.KEY_LATITUDE,
      ContentProvider.KEY_LONGITUDE,
      ContentProvider.KEY_DEPTH
    };

    List<String> selection = new ArrayList<>();

    if (minimumMagnitude >= 0) {
      selection.add('(' + ContentProvider.KEY_MAGNITUDE + " > " + minimumMagnitude + ')');
    }

    if (recordsCount < 0) {
      LocalDateTime dateTime = LocalDateTime.now();

      switch (recordsCount) {
        case PreferencesActivity.PREF_LAST_WEEK_RECORDS_VALUE:
          dateTime = dateTime.minusWeeks(1);
          break;
        case PreferencesActivity.PREF_LAST_MONTH_RECORDS_VALUE:
          dateTime = dateTime.minusMonths(1);
          break;
        case PreferencesActivity.PREF_LAST_QUARTER_RECORDS_VALUE:
          dateTime = dateTime.minusMonths(3);
          break;
        case PreferencesActivity.PREF_LAST_HALF_YEAR_RECORDS_VALUE:
          dateTime = dateTime.minusMonths(6);
          break;
        case PreferencesActivity.PREF_LAST_YEAR_RECORDS_VALUE:
          dateTime = dateTime.minusYears(1);
          break;
      }

      selection.add("(" + ContentProvider.KEY_DATE + " >= " + dateTime.toLocalDate().toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
    }

    String sortOrder = ContentProvider.KEY_DATE + " DESC";

    if (recordsCount > 0) {
      sortOrder += " LIMIT " + recordsCount;
    }

    return new CursorLoader(context, ContentProvider.CONTENT_URI, projection, TextUtils.join(" AND ", selection), null, sortOrder);
  }
}
