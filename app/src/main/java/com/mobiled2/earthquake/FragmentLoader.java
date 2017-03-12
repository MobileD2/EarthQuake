package com.mobiled2.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

class FragmentLoader {
  private static final String TAG = "FRAGMENT_LOADER";

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
      LocalDateTime startDateTime = LocalDateTime.now();
      LocalDateTime endDateTime = startDateTime.plusDays(1);

      switch (recordsCount) {
        case PreferencesActivity.PREF_YESTERDAY_RECORDS_VALUE:
          startDateTime = startDateTime.minusDays(1);
          endDateTime = endDateTime.minusDays(1);
          break;
        case PreferencesActivity.PREF_LAST_WEEK_RECORDS_VALUE:
          startDateTime = startDateTime.minusWeeks(1);
          break;
        case PreferencesActivity.PREF_LAST_MONTH_RECORDS_VALUE:
          startDateTime = startDateTime.minusMonths(1);
          break;
        case PreferencesActivity.PREF_LAST_QUARTER_RECORDS_VALUE:
          startDateTime = startDateTime.minusMonths(3);
          break;
        case PreferencesActivity.PREF_LAST_HALF_YEAR_RECORDS_VALUE:
          startDateTime = startDateTime.minusMonths(6);
          break;
        case PreferencesActivity.PREF_LAST_YEAR_RECORDS_VALUE:
          startDateTime = startDateTime.minusYears(1);
          break;
      }

      selection.add("(" + ContentProvider.KEY_DATE + " >= " + startDateTime.toLocalDate().toDateTime(LocalTime.MIDNIGHT).getMillis() +  " AND " + ContentProvider.KEY_DATE + " < " + endDateTime.toLocalDate().toDateTime(LocalTime.MIDNIGHT).getMillis() + ")");
    }

    String sortOrder = ContentProvider.KEY_DATE + " DESC";

    if (recordsCount > 0) {
      sortOrder += " LIMIT " + recordsCount;
    }

    return new CursorLoader(context, ContentProvider.CONTENT_URI, projection, TextUtils.join(" AND ", selection), null, sortOrder);
  }
}
