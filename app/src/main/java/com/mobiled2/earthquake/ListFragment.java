package com.mobiled2.earthquake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends android.support.v4.app.ListFragment implements IFragmentCallback, LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "LIST_FRAGMENT";

  private SharedPreferences prefs;
  private QuakeDataCursorAdapter adapter;
  private AppCompatActivity context;
  private Resources resources;

  @Override
  public void onAttach (Context context){
    super.onAttach(context);
    this.context = (AppCompatActivity)context;
    resources = context.getResources();
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

    intent.putExtra(ContentProvider.KEY_DATE, new LocalDateTime(cursor.getLong(cursor.getColumnIndex(ContentProvider.KEY_DATE))).toString("dd-MM-yyyy HH:mm"));
    intent.putExtra(ContentProvider.KEY_DETAILS, cursor.getString(cursor.getColumnIndex(ContentProvider.KEY_DETAILS)));
    intent.putExtra(ContentProvider.KEY_MAGNITUDE, cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_MAGNITUDE)));
    intent.putExtra(ContentProvider.KEY_LATITUDE, cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LATITUDE)));
    intent.putExtra(ContentProvider.KEY_LONGITUDE, cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_LONGITUDE)));
    intent.putExtra(ContentProvider.KEY_DEPTH, cursor.getDouble(cursor.getColumnIndex(ContentProvider.KEY_DEPTH)));

    ((IFragmentCallback)context).onFragmentClick(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return FragmentLoader.createLoader(context, prefs);
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
