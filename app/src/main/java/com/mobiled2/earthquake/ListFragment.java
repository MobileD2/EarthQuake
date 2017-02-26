package com.mobiled2.earthquake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListFragment extends android.support.v4.app.ListFragment implements IFragmentCallback, LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "LIST_FRAGMENT";

  private SharedPreferences prefs;
  QuakeDataCursorAdapter adapter;
  AppCompatActivity context;

  @Override
  public void onAttach (Context context){
    super.onAttach(context);
    this.context = (AppCompatActivity)context;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    prefs = PreferenceManager.getDefaultSharedPreferences(context);
    adapter = new QuakeDataCursorAdapter(context, R.layout.quake_data_list_item, null, new String[] { ContentProvider.KEY_DATE, ContentProvider.KEY_MAGNITUDE, ContentProvider.KEY_DETAILS }, new int[] { R.id.date, R.id.magnitude, R.id.details }, 0);

    setListAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public void onStart() {
    super.onStart();
    getLoaderManager().restartLoader(0, null, ListFragment.this);
    context.startService(new Intent(context, UpdateService.class));
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    Cursor cursor = (Cursor)getListAdapter().getItem(position);
    Intent intent = new Intent();

    intent.putExtra(ContentProvider.KEY_DATE, new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(cursor.getLong(cursor.getColumnIndex(ContentProvider.KEY_DATE))));
    intent.putExtra(ContentProvider.KEY_DETAILS, cursor.getString(cursor.getColumnIndex(ContentProvider.KEY_DETAILS)));
    intent.putExtra(ContentProvider.KEY_MAGNITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_MAGNITUDE))));
    intent.putExtra(ContentProvider.KEY_LATITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LATITUDE))));
    intent.putExtra(ContentProvider.KEY_LONGITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LONGITUDE))));
    intent.putExtra(ContentProvider.KEY_DEPTH, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_DEPTH))));

    ((IFragmentCallback)context).onFragmentClick(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    float minimumMagnitude = Float.parseFloat(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "-1"));
    int recordsCount = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_RECORDS_COUNT, "-1"));

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
      switch (recordsCount) {
        case -1:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW'), 'UTC'))");
        break;
        case -2:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW', '-7 DAYS'), 'UTC'))");
        break;
        case -3:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW', '-1 MONTH'), 'UTC'))");
        break;
        case -4:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW', '-3 MONTHS'), 'UTC'))");
        break;
        case -5:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW', '-6 MONTHS'), 'UTC'))");
        break;
        case -6:
          selection.add("(" + ContentProvider.KEY_DATE + " >= STRFTIME('%s000', DATE('NOW', '-1 YEAR'), 'UTC'))");
        break;
      }
    }

    String sortOrder = ContentProvider.KEY_DATE + " DESC";

    if (recordsCount > 0) {
      sortOrder += " LIMIT " + recordsCount;
    }

    return new CursorLoader(context, ContentProvider.CONTENT_URI, projection, TextUtils.join(" AND ", selection), null, sortOrder);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    adapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    adapter.swapCursor(null);
  }

  @Override
  public void onFragmentClick(Intent intent) {

  }
}
