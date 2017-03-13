package com.mobiled2.earthquake;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class EarthquakesWidget extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      Intent intent = new Intent(context, EarthquakesRemoteViewsService.class);

      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
      intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);

      views.setRemoteAdapter(R.id.widget_list_view, intent);
      views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text);

      Intent templateIntent = new Intent(context, MainActivity.class);

      templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

      PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0, templateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      views.setPendingIntentTemplate(R.id.widget_list_view, templatePendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    super.onUpdate(context, appWidgetManager, appWidgetIds);
  }
}
