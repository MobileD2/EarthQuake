package com.mobiled2.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UpdateService extends IntentService {
  private static final String TAG = "EARTHQUAKE_SERVICE";

  public static final int NOTIFICATION_ID = 1;

  private AlarmManager alarmManager;
  private PendingIntent alarmIntent;

  private NotificationCompat.Builder notificationBuilder;

  public UpdateService() {
    super("com.mobiled2.earthquake.UpdateService");
  }

  public UpdateService(String name) {
    super(name);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(AlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM), 0);
    notificationBuilder = new NotificationCompat.Builder(this);

    notificationBuilder.setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setSmallIcon(R.drawable.ic_notification)
      .setTicker("Earthquake detected");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    int updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));

    if (prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)) {
      alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + updateFreq * 60 * 1000, updateFreq * 60 * 1000, alarmIntent);
    } else {
      alarmManager.cancel(alarmIntent);
    }

    refreshEarthquakes();
  }

  public void refreshEarthquakes() {
    try {
      String quakeFeed = getString(R.string.quake_all_day_feed);
      URL url = new URL(quakeFeed);

      URLConnection connection;
      connection = url.openConnection();

      HttpURLConnection httpConnection = (HttpURLConnection)connection;
      int responseCode = httpConnection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream in = httpConnection.getInputStream();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document dom = db.parse(in);
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("event");

        if (nl.getLength() > 0) {
          for (int i = 0; i < nl.getLength(); ++i) {
            final QuakeData quake = new QuakeML().parse((Element)nl.item(i));

            if (quake != null) {
              addNewQuake(quake);
            }
          }
        }
      }

    } catch (MalformedURLException e) {
      Log.d(TAG, "MalformedURLException");
    } catch (IOException e) {
      Log.d(TAG, "IOException");
    } catch (ParserConfigurationException e) {
      Log.d(TAG, "ParserConfigurationException");
    } catch (SAXException e) {
      Log.d(TAG, "SAXException");
    }
  }

  private void addNewQuake(QuakeData quake) {
    ContentResolver contentResolver = getContentResolver();
    Cursor cursor = contentResolver.query(ContentProvider.CONTENT_URI, null, ContentProvider.KEY_DATE + "=" + quake.getDate().getTime(), null, null);

    if (cursor != null) {
      try {
        if (cursor.getCount() == 0) {
          ContentValues values = new ContentValues();

          values.put(ContentProvider.KEY_DATE, quake.getDate().getTime());
          values.put(ContentProvider.KEY_DETAILS, quake.getDetails());
          values.put(ContentProvider.KEY_SUMMARY, quake.toString());
          values.put(ContentProvider.KEY_LATITUDE, quake.getLatitude());
          values.put(ContentProvider.KEY_LONGITUDE, quake.getLongitude());
          values.put(ContentProvider.KEY_DEPTH, quake.getDepth());
          values.put(ContentProvider.KEY_MAGNITUDE, quake.getMagnitude());

          broadcastNotification(quake);

          contentResolver.insert(ContentProvider.CONTENT_URI, values);
        }
      } finally {
        cursor.close();
      }
    }
  }

  private void broadcastNotification(QuakeData quakeData) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    if (prefs.getBoolean(PreferencesActivity.PREF_NOTIFICATION, false)) {
      PendingIntent launchIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

      notificationBuilder.setContentIntent(launchIntent)
        .setWhen(quakeData.getDate().getTime())
        .setContentTitle("Magnitude: " + quakeData.getMagnitude())
        .setContentText(quakeData.getDetails());

      if (quakeData.getMagnitude() > 6) {
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
      }

      double vibrateLength = 100 * Math.exp(0.53 * quakeData.getMagnitude());

      notificationBuilder.setVibrate(new long[] { 100, 100, (long)vibrateLength });

      int color;

      if (quakeData.getMagnitude() < 5.4) {
        color = Color.GREEN;
      } else if (quakeData.getMagnitude() < 6) {
        color = Color.YELLOW;
      } else {
        color = Color.RED;
      }

      notificationBuilder.setLights(color, (int)vibrateLength, (int)vibrateLength);

      NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

      notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
  }
}
