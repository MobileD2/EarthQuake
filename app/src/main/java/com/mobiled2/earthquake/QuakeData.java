package com.mobiled2.earthquake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class QuakeData {
  private Date date;
  private String details;
  private double latitude;
  private double longitude;
  private double magnitude;

  QuakeData(Date date, String details, double latitude, double longitude, double magnitude) {
    this.date = date;
    this.details = details;
    this.latitude = latitude;
    this.longitude = longitude;
    this.magnitude = magnitude;
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

  double getLatitude() {
    return latitude;
  }

  double getLongitude() {
    return longitude;
  }

  double getMagnitude() {
    return magnitude;
  }
}
