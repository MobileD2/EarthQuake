package com.mobiled2.earthquake;

import android.location.Location;
import android.util.Log;

import org.w3c.dom.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

class QuakeML {
  private static final String TAG = "EARTHQUAKE";

  Quake parse(Element entry) {
    Element originEl = (Element)entry.getElementsByTagName("origin").item(0);

    Element description = (Element)entry.getElementsByTagName("description").item(0);

    String time = ((Element)originEl.getElementsByTagName("time").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
    Date qdate = new GregorianCalendar(0, 0, 0).getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.getDefault());

    try {
      qdate = sdf.parse(time);
    } catch (ParseException e) {
      Log.d(TAG, "Date parsing exception", e);
    }

    String details = description.getElementsByTagName("text").item(0).getFirstChild().getNodeValue();

    Location location = new Location("dummyGPS");
    String longitude = ((Element)originEl.getElementsByTagName("longitude").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
    String latitude = ((Element)originEl.getElementsByTagName("latitude").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue();

    location.setLongitude(Double.parseDouble(longitude));
    location.setLatitude(Double.parseDouble(latitude));

    Element magnitudeEl = (Element)entry.getElementsByTagName("magnitude").item(0);
    Double magnitude = Double.parseDouble(((Element)magnitudeEl.getElementsByTagName("mag").item(0)).getElementsByTagName("value").item(0).getFirstChild().getNodeValue());

    return new Quake(qdate, details, location, magnitude);
  }
}
