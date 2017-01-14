package com.mobiled2.earthquake;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Quake {
  private Date date;
  private String details;
  private Location location;
  private double magnitude;

  Quake(Date _d, String _det, Location _loc, double _mag) {
    date = _d;
    details = _det;
    location = _loc;
    magnitude = _mag;
  }

  @Override
  public String toString() {
    return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date) + ", " + magnitude + ", " + details;
  }

  Date getDate() {
    return date;
  }

  String getDetails() {
    return details;
  }

  Location getLocation() {
    return location;
  }

  double getMagnitude() {
    return magnitude;
  }
}
