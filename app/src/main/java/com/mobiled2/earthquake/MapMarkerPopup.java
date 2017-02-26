package com.mobiled2.earthquake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import java.util.HashMap;

class MapMarkerPopup implements GoogleMap.InfoWindowAdapter {
  private Context context = null;
  private LayoutInflater inflater = null;
  private HashMap<Marker, Uri> images = null;

  private int iconWidth;
  private int iconHeight;

  private Marker lastMarker = null;

  private View popup = null;

  MapMarkerPopup(Context context, LayoutInflater inflater, HashMap<Marker, Uri> images) {
    this.context = context;
    this.inflater = inflater;
    this.images = images;

    iconWidth = context.getResources().getDimensionPixelSize(R.dimen.icon_width);
    iconHeight = context.getResources().getDimensionPixelSize(R.dimen.icon_height);
  }

  @Override
  public View getInfoWindow(Marker marker) {
    return null;
  }

  @Override
  @SuppressLint("InflateParams")
  public View getInfoContents(Marker marker) {
    if (popup == null) {
      popup = inflater.inflate(R.layout.map_marker_popup, null);
    }

    if (lastMarker == null || !lastMarker.equals(marker)) {
      lastMarker = marker;

      TextView title = (TextView) popup.findViewById(R.id.map_marker_title);
      title.setText(marker.getTitle());

      TextView snippet = (TextView) popup.findViewById(R.id.map_marker_snippet);
      snippet.setText(marker.getSnippet());

      Uri image = images.get(marker);
      ImageView icon = (ImageView) popup.findViewById(R.id.map_marker_icon);

      if (image == null) {
        icon.setVisibility(View.GONE);
      } else {
        icon.setVisibility(View.VISIBLE);

        Picasso
          .with(context)
          .load(image)
          .resize(iconWidth, iconHeight)
          .centerCrop()
          .noFade()
          .placeholder(R.drawable.placeholder)
          .into(icon, new MarkerCallback(marker));
      }
    }

    return popup;
  }

  private static class MarkerCallback implements Callback {
    Marker marker = null;

    MarkerCallback(Marker marker) {
      this.marker = marker;
    }

    @Override
    public void onSuccess() {
      if (marker != null && marker.isInfoWindowShown()) {
        marker.showInfoWindow();
      }
    }

    @Override
    public void onError() {
      Log.e(getClass().getSimpleName(), "Error loading thumbnail");
    }
  }
}
