package com.mobiled2.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
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

public class UpdateService extends IntentService {
  private static final String TAG = "EARTHQUAKE_SERVICE";

  private AlarmManager alarmManager;
  private PendingIntent alarmIntent;

  private Notification notification;

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
    notification = new Notification(this);
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
        int listLength = nl.getLength();

        if (listLength > 0) {
          for (int i = listLength - 1; i >= 0; --i) {
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

  private void addNewQuake(QuakeData quakeData) {
    ContentResolver contentResolver = getContentResolver();
    Cursor cursor = contentResolver.query(ContentProvider.CONTENT_URI, null, ContentProvider.KEY_DATE + "=" + quakeData.getDate().getTime(), null, null);

    if (cursor != null) {
      try {
        if (cursor.getCount() == 0) {
          ContentValues values = new ContentValues();

          values.put(ContentProvider.KEY_DATE, quakeData.getDate().getTime());
          values.put(ContentProvider.KEY_DETAILS, quakeData.getDetails());
          values.put(ContentProvider.KEY_SUMMARY, quakeData.toString());
          values.put(ContentProvider.KEY_LATITUDE, quakeData.getLatitude());
          values.put(ContentProvider.KEY_LONGITUDE, quakeData.getLongitude());
          values.put(ContentProvider.KEY_DEPTH, quakeData.getDepth());
          values.put(ContentProvider.KEY_MAGNITUDE, quakeData.getMagnitude());

          if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PreferencesActivity.PREF_NOTIFICATION, false)) {
            notification.send(quakeData);
          }

          contentResolver.insert(ContentProvider.CONTENT_URI, values);
        }
      } finally {
        cursor.close();
      }
    }
  }
}
