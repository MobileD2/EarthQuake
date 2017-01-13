package com.mobiled2.earthquake;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeListFragment extends ListFragment {
  private static final String TAG = "EARTHQUAKE";

  private ArrayList<Quake> earthquakes = new ArrayList<>();
  private ArrayAdapter<Quake> aa;

  private Handler handler = new Handler();

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    aa = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, earthquakes);

    setListAdapter(aa);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        refreshEarthquakes();
      }
    });

    t.start();
  }

  public void refreshEarthquakes() {
    URL url;

    try {
      String quakeFeed = getString(R.string.quake_all_day_feed);
      url = new URL(quakeFeed);

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

        earthquakes.clear();

        NodeList nl = docEle.getElementsByTagName("event");

        if (nl.getLength() > 0) {
          for (int i = 0; i < nl.getLength(); ++i) {
            final Quake quake = new QuakeML().parse((Element)nl.item(i));

            handler.post(new Runnable() {
              @Override
              public void run() {
                addNewQuake(quake);
              }
            });
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
    MainActivity earthquakeActivity = (MainActivity) getActivity();

    if (_quake.getMagnitude() > earthquakeActivity.minimumMagnitude) {
      earthquakes.add(_quake);
    }

    aa.notifyDataSetChanged();
  }
}
