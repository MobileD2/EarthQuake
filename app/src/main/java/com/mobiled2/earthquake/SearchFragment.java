package com.mobiled2.earthquake;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import org.joda.time.LocalDateTime;

public class SearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  public static final String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

  private static final String TAG = "SEARCH_FRAGMENT";

  QuakeDataCursorAdapter adapter;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new QuakeDataCursorAdapter(getActivity(), R.layout.quake_data_list_item, null, new String[] { ContentProvider.KEY_DATE, ContentProvider.KEY_MAGNITUDE, ContentProvider.KEY_DETAILS }, new int[] { R.id.date, R.id.magnitude, R.id.details }, 0);
    setListAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);

    restartLoader(getActivity().getIntent());
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    Cursor cursor = (Cursor)getListAdapter().getItem(position);
    Intent intent = new Intent(getActivity(), QuakeDetailsActivity.class);

    intent.putExtra(ContentProvider.KEY_DATE, new LocalDateTime(cursor.getLong(cursor.getColumnIndex(ContentProvider.KEY_DATE))).toString("dd-MM-yyyy HH:mm"));
    intent.putExtra(ContentProvider.KEY_DETAILS, cursor.getString(cursor.getColumnIndex(ContentProvider.KEY_DETAILS)));
    intent.putExtra(ContentProvider.KEY_MAGNITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_MAGNITUDE))));
    intent.putExtra(ContentProvider.KEY_LATITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LATITUDE))));
    intent.putExtra(ContentProvider.KEY_LONGITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LONGITUDE))));
    intent.putExtra(ContentProvider.KEY_DEPTH, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_DEPTH))));

    startActivity(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String query = "";

    if (args != null) {
      query = args.getString(QUERY_EXTRA_KEY);
    }

    String[] projection = {
      ContentProvider.KEY_ID,
      ContentProvider.KEY_DATE,
      ContentProvider.KEY_MAGNITUDE,
      ContentProvider.KEY_DETAILS,
      ContentProvider.KEY_LATITUDE,
      ContentProvider.KEY_LONGITUDE,
      ContentProvider.KEY_DEPTH,
      ContentProvider.KEY_SUMMARY
    };

    String selection = ContentProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
    String sortOrder = ContentProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

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

  public void restartLoader(Intent intent) {
    Bundle bundle = new Bundle();

    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      bundle.putString(QUERY_EXTRA_KEY, intent.getStringExtra(SearchManager.QUERY));
      getLoaderManager().restartLoader(0, bundle, this);
    }

    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      bundle.putString(QUERY_EXTRA_KEY, intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
      getLoaderManager().restartLoader(0, bundle, this);
    }
  }
}
