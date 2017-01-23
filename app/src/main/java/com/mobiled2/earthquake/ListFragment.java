package com.mobiled2.earthquake;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ListFragment extends android.support.v4.app.ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "EARTHQUAKE_FRAGMENT";
  private static final int RECORDS_COUNT = 100;

  QuakeDataCursorAdapter adapter;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new QuakeDataCursorAdapter(getActivity(), R.layout.quake_data_list_item, null, new String[] { ContentProvider.KEY_DATE, ContentProvider.KEY_MAGNITUDE, ContentProvider.KEY_DETAILS }, new int[] { R.id.date, R.id.magnitude, R.id.details }, 0);
    setListAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);

    refreshEarthquakes();
  }

  public void refreshEarthquakes() {
    getLoaderManager().restartLoader(0, null, ListFragment.this);
    getActivity().startService(new Intent(getActivity(), UpdateService.class));
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    Cursor cursor = (Cursor)getListAdapter().getItem(position);
    Intent intent = new Intent(getActivity(), QuakeDetailsActivity.class);

    intent.putExtra(ContentProvider.KEY_DATE, new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(cursor.getLong(cursor.getColumnIndex(ContentProvider.KEY_DATE))));
    intent.putExtra(ContentProvider.KEY_DETAILS, cursor.getString(cursor.getColumnIndex(ContentProvider.KEY_DETAILS)));
    intent.putExtra(ContentProvider.KEY_MAGNITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_MAGNITUDE))));
    intent.putExtra(ContentProvider.KEY_LATITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LATITUDE))));
    intent.putExtra(ContentProvider.KEY_LONGITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LONGITUDE))));
    intent.putExtra(ContentProvider.KEY_DEPTH, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_DEPTH))));

    startActivity(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    MainActivity mainActivity = (MainActivity)getActivity();

    String[] projection = new String[] {
      ContentProvider.KEY_ID,
      ContentProvider.KEY_DATE,
      ContentProvider.KEY_MAGNITUDE,
      ContentProvider.KEY_DETAILS,
      ContentProvider.KEY_LATITUDE,
      ContentProvider.KEY_LONGITUDE,
      ContentProvider.KEY_DEPTH
    };

    String selection = ContentProvider.KEY_MAGNITUDE + " > " + mainActivity.minimumMagnitude;
    String sortOrder = ContentProvider.KEY_DATE + " DESC LIMIT " + RECORDS_COUNT;

    return new CursorLoader(getActivity(), ContentProvider.CONTENT_URI, projection, selection, null, sortOrder);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    adapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    adapter.swapCursor(null);
  }
}
