package com.mobiled2.earthquake;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "EARTHQUAKE_FRAGMENT";
  private static final int RECORDS_COUNT = 100;

  QuakeDataCursorAdapter adapter;
  Activity context;

  @Override
  public void onAttach (Context context){
    super.onAttach(context);
    this.context = (Activity)context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.list_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new QuakeDataCursorAdapter(context, R.layout.quake_data_list_item, null, new String[] { ContentProvider.KEY_DATE, ContentProvider.KEY_MAGNITUDE, ContentProvider.KEY_DETAILS }, new int[] { R.id.date, R.id.magnitude, R.id.details }, 0);

    ListView listView = (ListView)context.findViewById(R.id.list_fragment_view);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor)adapter.getItem(position);
        Intent intent = new Intent(context, QuakeDetailsActivity.class);

        intent.putExtra(ContentProvider.KEY_DATE, new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(cursor.getLong(cursor.getColumnIndex(ContentProvider.KEY_DATE))));
        intent.putExtra(ContentProvider.KEY_DETAILS, cursor.getString(cursor.getColumnIndex(ContentProvider.KEY_DETAILS)));
        intent.putExtra(ContentProvider.KEY_MAGNITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_MAGNITUDE))));
        intent.putExtra(ContentProvider.KEY_LATITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LATITUDE))));
        intent.putExtra(ContentProvider.KEY_LONGITUDE, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LONGITUDE))));
        intent.putExtra(ContentProvider.KEY_DEPTH, String.valueOf(cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_DEPTH))));

        startActivity(intent);
      }
    });

    initSearchView();

    getLoaderManager().initLoader(0, null, this);

    refreshEarthquakes(context);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    MainActivity mainActivity = (MainActivity)context;

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

    return new CursorLoader(context, ContentProvider.CONTENT_URI, projection, selection, null, sortOrder);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    adapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    adapter.swapCursor(null);
  }

  private void initSearchView() {
    SearchManager searchManager = (SearchManager)context.getSystemService(Context.SEARCH_SERVICE);
    SearchableInfo searchableInfo = searchManager.getSearchableInfo(context.getComponentName());
    SearchView searchView = (SearchView)context.findViewById(R.id.searchView);

    searchView.setSearchableInfo(searchableInfo);
  }

  private void refreshEarthquakes(Activity activity) {
    getLoaderManager().restartLoader(0, null, ListFragment.this);
    activity.startService(new Intent(activity, UpdateService.class));
  }
}
