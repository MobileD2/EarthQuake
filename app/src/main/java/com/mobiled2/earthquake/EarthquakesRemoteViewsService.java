package com.mobiled2.earthquake;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.LocalDateTime;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class EarthquakesRemoteViewsService extends RemoteViewsService {
  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new EarthquakesRemoteViewsFactory(getApplicationContext());
  }

  private class EarthquakesRemoteViewsFactory implements RemoteViewsFactory {
    private Context context;
    private SharedPreferences prefs;
    private Cursor cursor;

    EarthquakesRemoteViewsFactory(Context context) {
      this.context = context;
      this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreate() {
      cursor = executeQuery();
    }

    @Override
    public void onDataSetChanged() {
      cursor = executeQuery();
    }

    @Override
    public void onDestroy() {
      cursor.close();
    }

    @Override
    public int getCount() {
      return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
      if (cursor != null) {
        cursor.moveToPosition(position);

        String id = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_ID));
        String magnitude = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_MAGNITUDE));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LONGITUDE));
        String depth = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DEPTH));
        String details = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DETAILS));
        String date = new LocalDateTime(cursor.getLong(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DATE))).toString("dd-MM-yyyy HH:mm");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

        remoteViews.setTextViewText(R.id.widget_magnitude, magnitude);
        remoteViews.setTextViewText(R.id.widget_details, details + "\n" + date);
        remoteViews.setTextViewText(R.id.widget_depth, depth);

        Intent fillInIntent = new Intent();
        Uri uri = Uri.withAppendedPath(ContentProvider.CONTENT_URI, "earthquakes/" + id);

        fillInIntent.putExtra(ContentProvider.KEY_LATITUDE, latitude);
        fillInIntent.putExtra(ContentProvider.KEY_LONGITUDE, longitude);
        fillInIntent.setData(uri);

        remoteViews.setOnClickFillInIntent(R.id.widget_magnitude, fillInIntent);
        remoteViews.setOnClickFillInIntent(R.id.widget_details, fillInIntent);

        return remoteViews;
      }

      return null;
    }

    @Override
    public RemoteViews getLoadingView() {
      return null;
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public long getItemId(int position) {
      return cursor != null ? cursor.getLong(cursor.getColumnIndexOrThrow(ContentProvider.KEY_ID)) : position;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    private Cursor executeQuery() {
      CursorLoaderQuery cursorLoaderQuery = new CursorLoaderQuery(prefs);

      return context.getContentResolver().query(ContentProvider.CONTENT_URI, cursorLoaderQuery.getProjection(), cursorLoaderQuery.getSelection(), null, cursorLoaderQuery.getSortOrder());
    }
  }
}
