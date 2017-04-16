package com.mobiled2.earthquake;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;

import org.joda.time.LocalDateTime;

class Notification {
  private static final int NOTIFICATION_ID = 1;

  private static final double LIGHT_MAGNITUDE = 3;
  private static final double MEDIUM_MAGNITUDE = 5;

  private static final int LIGHT_MAGNITUDE_COLOR = Color.GREEN;
  private static final int MEDIUM_MAGNITUDE_COLOR = Color.YELLOW;
  private static final int HEAVY_MAGNITUDE_COLOR = Color.RED;

  private NotificationCompat.Builder notificationBuilder;
  private Context context;

  Notification(Context context) {
    notificationBuilder = new NotificationCompat.Builder(context);

    notificationBuilder
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setSmallIcon(R.drawable.ic_notification)
      .setTicker("Earthquake detected");

    this.context = context;
  }

  void send(QuakeData quakeData) {
    Intent intent = new Intent(context, MainActivity.class);
    long time = quakeData.getDate().getTime();
    String date = new LocalDateTime(time).toString("dd-MM-yyyy HH:mm");
    String details = quakeData.getDetails();
    double magnitude = quakeData.getMagnitude();

    intent.putExtra(ContentProvider.KEY_DATE, date);
    intent.putExtra(ContentProvider.KEY_DETAILS, details);
    intent.putExtra(ContentProvider.KEY_MAGNITUDE, String.valueOf(magnitude));
    intent.putExtra(ContentProvider.KEY_LATITUDE, String.valueOf(quakeData.getLatitude()));
    intent.putExtra(ContentProvider.KEY_LONGITUDE, String.valueOf(quakeData.getLongitude()));
    intent.putExtra(ContentProvider.KEY_DEPTH, String.valueOf(quakeData.getDepth()));

    PendingIntent launchIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    notificationBuilder
      .setContentIntent(launchIntent)
      .setContentTitle("Magnitude: " + magnitude)
      .setContentText(details)
      .setSubText(date)
      .setWhen(time);

    if (magnitude > MEDIUM_MAGNITUDE) {
      notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    double vibrateLength = 100 * Math.exp(0.53 * magnitude);

    notificationBuilder.setVibrate(new long[] { 100, 100, (long)vibrateLength });

    int color;

    if (magnitude < LIGHT_MAGNITUDE) {
      color = LIGHT_MAGNITUDE_COLOR;
    } else if (magnitude < MEDIUM_MAGNITUDE) {
      color = MEDIUM_MAGNITUDE_COLOR;
    } else {
      color = HEAVY_MAGNITUDE_COLOR;
    }

    notificationBuilder.setLights(color, (int)vibrateLength, (int)vibrateLength);

    NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
  }
}
