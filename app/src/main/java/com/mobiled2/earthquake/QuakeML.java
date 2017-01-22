package com.mobiled2.earthquake;

import android.util.Log;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

class QuakeML {
  private static final String TAG = "EARTHQUAKE_QUAKEML";

  QuakeData parse(Element entry) {
    if (entry == null) {
      return null;
    }

    NodeList originElNodeList = entry.getElementsByTagName("origin");

    if (originElNodeList == null) {
      return null;
    }

    Element originEl = (Element)originElNodeList.item(0);

    if (originEl == null) {
      return null;
    }

    NodeList descriptionNode = entry.getElementsByTagName("description");

    if (descriptionNode == null) {
      return null;
    }

    Element description = (Element)descriptionNode.item(0);

    if (description == null) {
      return null;
    }

    NodeList detailsNodeList = description.getElementsByTagName("text");

    if (detailsNodeList == null) {
      return null;
    }

    Element detailsEl = (Element)detailsNodeList.item(0);

    if (detailsEl == null) {
      return null;
    }

    Node detailsChildNode = detailsEl.getFirstChild();

    if (detailsChildNode == null) {
      return null;
    }

    String details = detailsChildNode.getNodeValue();

    if (details == null) {
      return null;
    }

    String time = parseStringNodeList(originEl.getElementsByTagName("time"));

    if (time == null) {
      return null;
    }

    String longitude = parseStringNodeList(originEl.getElementsByTagName("longitude"));

    if (longitude == null) {
      return null;
    }

    String latitude = parseStringNodeList(originEl.getElementsByTagName("latitude"));

    if (latitude == null) {
      return null;
    }

    NodeList magnitudeNodeList = entry.getElementsByTagName("magnitude");

    if (magnitudeNodeList == null) {
      return null;
    }

    Element magnitudeEl = (Element)magnitudeNodeList.item(0);

    if (magnitudeEl == null) {
      return null;
    }

    String magnitude = parseStringNodeList(magnitudeEl.getElementsByTagName("mag"));

    if (magnitude == null) {
      return null;
    }

    Date date = new GregorianCalendar(0, 0, 0).getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.getDefault());

    try {
      date = sdf.parse(time);
    } catch (ParseException e) {
      Log.d(TAG, "Date parsing exception", e);
    }

    return new QuakeData(date, details, Double.parseDouble(latitude), Double.parseDouble(longitude), Double.parseDouble(magnitude));
  }

  private String parseStringNodeList(NodeList entry) {
    if (entry == null) {
      return null;
    }

    Element entryEl = (Element)entry.item(0);

    if (entryEl == null) {
      return null;
    }

    NodeList valueNodeList = entryEl.getElementsByTagName("value");

    if (valueNodeList == null) {
      return null;
    }

    Element valueEl = (Element)valueNodeList.item(0);

    if (valueEl == null) {
      return null;
    }

    Node valueChildNode = valueEl.getFirstChild();

    if (valueChildNode == null) {
      return null;
    }

    return valueChildNode.getNodeValue();
  }
}
