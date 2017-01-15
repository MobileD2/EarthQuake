package com.mobiled2.earthquake;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

public class EarthquakeSearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  public static final String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

  private static final String TAG = "SEARCH_FRAGMENT";

  SimpleCursorAdapter adapter;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, new String[] { EarthquakeContentProvider.KEY_SUMMARY }, new int[] { android.R.id.text1 }, 0);
    setListAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);

    restartLoader(getActivity().getIntent());
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String query = "";

    if (args != null) {
      query = args.getString(QUERY_EXTRA_KEY);
    }

    String[] projection = {
      EarthquakeContentProvider.KEY_ID,
      EarthquakeContentProvider.KEY_SUMMARY
    };

    String selection = EarthquakeContentProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
    String sortOrder = EarthquakeContentProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

    return new CursorLoader(getActivity(), EarthquakeContentProvider.CONTENT_URI, projection, selection, null, sortOrder);
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
