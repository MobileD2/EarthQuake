package com.mobiled2.earthquake;

import org.joda.time.LocalDateTime;

import java.util.Date;

class QuakeData {
  private Date date;
  private String details;
  private double latitude;
  private double longitude;
  private double depth;
  private double magnitude;

  QuakeData(Date date, String details, double latitude, double longitude, double depth, double magnitude) {
    this.date = date;
    this.details = details;
    this.latitude = latitude;
    this.longitude = longitude;
    this.depth = depth;
    this.magnitude = magnitude;
  }

  @Override
  public String toString() {
    return new LocalDateTime(date.getTime()).toString("HH:mm") + ", " + magnitude + ", " + details;
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

  double getDepth() {
    return depth;
  }

  double getMagnitude() {
    return magnitude;
  }
}
