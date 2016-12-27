package com.mobiled2.earthquake;

import android.location.Location;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    aa = new ArrayAdapter<Quake>(getActivity(), android.R.layout.simple_list_item_1, earthquakes);

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
            Element entry = (Element)nl.item(i);

            Element description = (Element)entry.getElementsByTagName("description").item(0);
            String details = description.getElementsByTagName("text").item(0).getFirstChild().getNodeValue();

            Element originEl = (Element)entry.getElementsByTagName("origin").item(0);

            String time = ((Element)originEl.getElementsByTagName("time").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
            Date qdate = new GregorianCalendar(0, 0, 0).getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.getDefault());

            try {
              qdate = sdf.parse(time);
            } catch (ParseException e) {
              Log.d(TAG, "Date parsing exception", e);
            }

            Location location = new Location("dummyGPS");
            String longitude = ((Element)originEl.getElementsByTagName("longitude").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
            String latitude = ((Element)originEl.getElementsByTagName("latitude").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();

            location.setLongitude(Double.parseDouble(longitude));
            location.setLatitude(Double.parseDouble(latitude));

            Element magnitudeEl = (Element)entry.getElementsByTagName("magnitude").item(0);
            String magnitude = ((Element)magnitudeEl.getElementsByTagName("mag").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();

            final Quake quake = new Quake(qdate, details, location, Double.parseDouble(magnitude));

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
    } finally {
    }
  }

  private void addNewQuake(Quake _quake) {
    earthquakes.add(_quake);
    aa.notifyDataSetChanged();
  }
}
