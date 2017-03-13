package com.mobiled2.earthquake;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.widget.RemoteViews;

import org.joda.time.LocalDateTime;

public class LastEarthquakeWidget extends AppWidgetProvider {
  private double latitude = Double.NaN;
  private double longitude = Double.NaN;

  public void updateQuake(Context context) {
    ComponentName componentName = new ComponentName(context, LastEarthquakeWidget.class);
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

    updateQuake(context, appWidgetManager, appWidgetIds);
  }

  public void updateQuake(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(ContentProvider.CONTENT_URI, null, null, null, null);
    Resources resources = context.getResources();
    String magnitude = resources.getString(R.string.widget_magnitude_empty_value);
    String details = resources.getString(R.string.widget_details_empty_value);
    String date = resources.getString(R.string.widget_date_empty_value);

    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LATITUDE));
          longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LONGITUDE));
          magnitude = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_MAGNITUDE));
          details = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DETAILS));
          date = new LocalDateTime(cursor.getLong(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DATE))).toString("dd-MM-yyyy HH:mm");
        }
      } finally {
        cursor.close();
      }
    }

    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

      views.setTextViewText(R.id.widget_magnitude, magnitude);
      views.setTextViewText(R.id.widget_details, details + "\n" + date);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    updateQuake(context, appWidgetManager, appWidgetIds);

    Intent intent = new Intent(context, MainActivity.class);

    intent.putExtra(ContentProvider.KEY_LATITUDE, latitude);
    intent.putExtra(ContentProvider.KEY_LONGITUDE, longitude);

    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

    views.setOnClickPendingIntent(R.id.widget_magnitude, pendingIntent);
    views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

    appWidgetManager.updateAppWidget(appWidgetIds, views);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    if (intent.getAction().equals(UpdateService.QUAKES_REFRESHED)) {
      updateQuake(context);
    }
  }
}
