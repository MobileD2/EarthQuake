package com.mobiled2.earthquake;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
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

public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "EARTHQUAKE_FRAGMENT";
  private static final int RECORDS_COUNT = 100;

  SimpleCursorAdapter adapter;

  private Handler handler = new Handler();

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, new String[] { EarthquakeContentProvider.KEY_SUMMARY }, new int[] { android.R.id.text1 }, 0);
    setListAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        refreshEarthquakes();
      }
    });

    t.start();
  }

  public void refreshEarthquakes() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
      }
    });

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
            final Quake quake = new QuakeML().parse((Element)nl.item(i));

            if (quake != null) {
              handler.post(new Runnable() {
                @Override
                public void run() {
                  addNewQuake(quake);
                }
              });
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

  private void addNewQuake(Quake _quake) {
    ContentResolver contentResolver = getActivity().getContentResolver();
    Cursor cursor = contentResolver.query(EarthquakeContentProvider.CONTENT_URI, null, EarthquakeContentProvider.KEY_DATE + "=" + _quake.getDate().getTime(), null, null);

    if (cursor != null) {
      try {
        if (cursor.getCount() == 0) {
          ContentValues values = new ContentValues();

          values.put(EarthquakeContentProvider.KEY_DATE, _quake.getDate().getTime());
          values.put(EarthquakeContentProvider.KEY_DETAILS, _quake.getDetails());
          values.put(EarthquakeContentProvider.KEY_SUMMARY, _quake.toString());
          values.put(EarthquakeContentProvider.KEY_LOCATION_LATITUDE, _quake.getLocation().getLatitude());
          values.put(EarthquakeContentProvider.KEY_LOCATION_LONGITUDE, _quake.getLocation().getLongitude());
          values.put(EarthquakeContentProvider.KEY_MAGNITUDE, _quake.getMagnitude());

          contentResolver.insert(EarthquakeContentProvider.CONTENT_URI, values);
        }
      } finally {
        cursor.close();
      }
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    MainActivity earthquakeActivity = (MainActivity)getActivity();

    String[] projection = new String[] {
      EarthquakeContentProvider.KEY_ID,
      EarthquakeContentProvider.KEY_SUMMARY
    };

    String selection = EarthquakeContentProvider.KEY_MAGNITUDE + " > " + earthquakeActivity.minimumMagnitude;
    String sortOrder = EarthquakeContentProvider.KEY_DATE + " DESC LIMIT " + RECORDS_COUNT;

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
}
