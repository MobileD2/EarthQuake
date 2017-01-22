package com.mobiled2.earthquake;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

public class UpdateService extends Service {
  private static final String TAG = "EARTHQUAKE_SERVICE";

  private AlarmManager alarmManager;
  private PendingIntent alarmIntent;

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
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    int updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));

    if (prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)) {
      alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + updateFreq * 60 * 1000, updateFreq * 60 * 1000, alarmIntent);
    } else {
      alarmManager.cancel(alarmIntent);
    }

    new Thread(new Runnable() {
      @Override
      public void run() {
        refreshEarthquakes();
      }
    }).start();

    return Service.START_NOT_STICKY;
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
    } finally {
      stopSelf();
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
          values.put(ContentProvider.KEY_LOCATION_LATITUDE, quake.getLatitude());
          values.put(ContentProvider.KEY_LOCATION_LONGITUDE, quake.getLongitude());
          values.put(ContentProvider.KEY_MAGNITUDE, quake.getMagnitude());

          contentResolver.insert(ContentProvider.CONTENT_URI, values);
        }
      } finally {
        cursor.close();
      }
    }
  }
}
