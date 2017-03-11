package com.mobiled2.earthquake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.util.Property;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends SupportMapFragment implements IFragmentCallback, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = "MAP_FRAGMENT";

  private Resources resources;
  private SharedPreferences prefs;
  private GoogleMap googleMap = null;

  private float minPrefMagnitude;
  private float maxPrefMagnitude;

  private HashMap<Marker, Uri> images = new HashMap<>();
  private AppCompatActivity context;

  private boolean needsInit = false;

  @Override
  public void onAttach (Context context){
    super.onAttach(context);
    this.context = (AppCompatActivity)context;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    resources = context.getResources();
    prefs = PreferenceManager.getDefaultSharedPreferences(context);

    String[] magnitudeValues = resources.getStringArray(R.array.magnitude_values);
    minPrefMagnitude = Float.parseFloat(magnitudeValues[0]);
    maxPrefMagnitude = Float.parseFloat(magnitudeValues[magnitudeValues.length - 1]);

    if (savedInstanceState == null) {
      needsInit = true;
    }

    setRetainInstance(true);
    getExtendedMapAsync(this);
  }

  @Override
  public void onStart() {
    super.onStart();

    if (googleMap != null) {
      setMapClustering(googleMap);
    }

    getLoaderManager().restartLoader(0, null, MapFragment.this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;

    setMapClustering(googleMap);

    googleMap.setInfoWindowAdapter(new MapMarkerPopup(this.context, context.getLayoutInflater(), images));
    googleMap.setOnInfoWindowClickListener(this);

    googleMap.setOnMarkerClickListener(this);
    googleMap.setOnMarkerDragListener(this);
    googleMap.setIndoorEnabled(true);

    getLoaderManager().initLoader(0, null, this);
  }

  private void setMapClustering(GoogleMap googleMap) {
    String clusteringColor = prefs.getString(PreferencesActivity.PREF_CLUSTERING_COLOR, "Mean");
    MapClusterOptionsProvider mapClusterOptionsProvider = new MapClusterOptionsProvider(context, context.getResources(), minPrefMagnitude, maxPrefMagnitude, BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_RED, clusteringColor);

    googleMap.setClustering(new ClusteringSettings().enabled(true).addMarkersDynamically(true).clusterOptionsProvider(mapClusterOptionsProvider));
  }

  private Marker addMarker(GoogleMap googleMap, float magnitude, LatLng position, String title, String snippet, boolean flat, float rotation, String image, float hue) {
    MarkerOptions markerOptions = new MarkerOptions()
      .position(position)
      .title(title)
      .snippet(snippet)
      .flat(flat)
      .rotation(rotation)
      .icon(BitmapDescriptorFactory.defaultMarker(hue))
      .data(magnitude)
      .draggable(false);

    Marker marker = googleMap.addMarker(markerOptions);

    if (image != null) {
      images.put(marker, Uri.parse(resources.getString(R.string.map_marker_image_url) + '/' + image));
    }

    return marker;
  }

  private void animateMarker(Marker markerToAnimate, LatLng animateTo, long duration) {
    Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
    ObjectAnimator animator = ObjectAnimator.ofObject(markerToAnimate, property, new LatLngEvaluator(), animateTo);

    animator.setDuration(duration);
    animator.start();
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    Toast.makeText(context, marker.getTitle(), Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    boolean isCluster = marker.isCluster();

    if (isCluster) {
      LatLngBounds.Builder builder = LatLngBounds.builder();

      for (Marker item : marker.getMarkers()) {
        builder.include(item.getPosition());
      }

      googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), getLatLngBoundsPadding(0.1)), 1000, null);
    }

    return isCluster;
  }

  @Override
  public void onMarkerDragStart(Marker marker) {

  }

  @Override
  public void onMarkerDrag(Marker marker) {

  }

  @Override
  public void onMarkerDragEnd(Marker marker) {

  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    float minimumMagnitude = Float.parseFloat(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "-1"));
    int recordsCount = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_RECORDS_COUNT, "-1"));

    String[] projection = new String[] {
      ContentProvider.KEY_ID,
      ContentProvider.KEY_DATE,
      ContentProvider.KEY_MAGNITUDE,
      ContentProvider.KEY_DETAILS,
      ContentProvider.KEY_LATITUDE,
      ContentProvider.KEY_LONGITUDE,
      ContentProvider.KEY_DEPTH
    };

    List<String> selection = new ArrayList<>();

    if (minimumMagnitude >= 0) {
      selection.add('(' + ContentProvider.KEY_MAGNITUDE + " > " + minimumMagnitude + ')');
    }

    if (recordsCount < 0) {
      switch (recordsCount) {
        case -1:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
        case -2:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().minusDays(7).toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
        case -3:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().minusMonths(1).toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
        case -4:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().minusMonths(3).toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
        case -5:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().minusMonths(6).toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
        case -6:
          selection.add("(" + ContentProvider.KEY_DATE + " >= " + LocalDateTime.now().toLocalDate().minusYears(1).toDateTime(LocalTime.MIDNIGHT).getMillis() +  ")");
        break;
      }
    }

    String sortOrder = ContentProvider.KEY_DATE + " DESC";

    if (recordsCount > 0) {
      sortOrder += " LIMIT " + recordsCount;
    }

    return new CursorLoader(context, ContentProvider.CONTENT_URI, projection, TextUtils.join(" AND ", selection), null, sortOrder);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    refreshLocations(googleMap, data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    refreshLocations(googleMap, null);
  }

  private void refreshLocations(GoogleMap googleMap, Cursor cursor) {
    if (googleMap != null) {
      googleMap.clear();

      if (cursor != null && cursor.moveToFirst()) {
        do {
          double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LATITUDE));
          double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(ContentProvider.KEY_LONGITUDE));
          LatLng position = new LatLng(latitude, longitude);
          String date = new LocalDateTime(cursor.getLong(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DATE))).toString("dd-MM-yyyy HH:mm");
          String details = cursor.getString(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DETAILS));
          float magnitude = cursor.getFloat(cursor.getColumnIndexOrThrow(ContentProvider.KEY_MAGNITUDE));
          float depth = cursor.getFloat(cursor.getColumnIndexOrThrow(ContentProvider.KEY_DEPTH));
          float hue = (((magnitude - maxPrefMagnitude) * BitmapDescriptorFactory.HUE_AZURE - (magnitude - minPrefMagnitude) * BitmapDescriptorFactory.HUE_RED) / (minPrefMagnitude - maxPrefMagnitude));

          addMarker(googleMap, magnitude, position, "Magnitude: " + magnitude, "Depth:\t\t" + depth + " m\nPlace:\t\t" + details + "\nDate:\t\t\t" + date, false, 0, null, hue);
        } while (cursor.moveToNext());

        if (needsInit) {
          context.findViewById(android.R.id.content).post(() -> zoomToArea(googleMap.getDisplayedMarkers(), false));
        }
      }
    }
  }

  @Override
  public void onFragmentClick(Intent intent) {
    if (googleMap != null) {
      int zoom = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_ZOOM_WHEN_ITEM_CLICKED, "-2"));
      LatLng position = new LatLng(intent.getDoubleExtra(ContentProvider.KEY_LATITUDE, 0), intent.getDoubleExtra(ContentProvider.KEY_LONGITUDE, 0));

      switch(zoom) {
        case -1:
          zoomToMaxValue(position, true);
        break;
        case -2:
          zoomToAutoValue(position, true);
        break;
        default:
          zoomToDefinedValue(position, zoom, true);
        break;
      }
    }
  }

  private void zoomToDefinedValue(LatLng position, int zoom, boolean animate) {
    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
      .target(position)
      .zoom(zoom)
      .build());

    try {
      if (animate) {
        googleMap.animateCamera(cameraUpdate, 1000, null);
      } else {
        googleMap.moveCamera(cameraUpdate);
      }
    } catch (IllegalStateException exception) {
      Log.e(TAG, exception.toString());
    }
  }

  private void zoomToMaxValue(LatLng position, boolean animate) {
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(position).build(), getLatLngBoundsPadding(0.1));

    try {
      if (animate) {
        googleMap.animateCamera(cameraUpdate, 1000, null);
      } else {
        googleMap.moveCamera(cameraUpdate);
      }
    } catch (IllegalStateException exception) {
      Log.e(TAG, exception.toString());
    }
  }

  private void zoomToArea(List<Marker> markers, boolean animate) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    for (Marker marker : markers) {
      builder.include(marker.getPosition());
    }

    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), getLatLngBoundsPadding(0.1));

    try {
      if (animate) {
        googleMap.animateCamera(cameraUpdate, 1000, null);
      } else {
        googleMap.moveCamera(cameraUpdate);
      }
    } catch (IllegalStateException exception) {
      Log.e(TAG, exception.toString());
    }
  }

  private void zoomToAutoValue(LatLng position, boolean animate) {
    List<LatLng> nearestMarkerPositions = new ArrayList<>();

    nearestMarkerPositions.add(position);

    LatLng nearestMarkerPosition = findNearestMarkerPosition(position, googleMap.getMarkers(), nearestMarkerPositions);
    LatLngBounds boundsArea = createBoundsArea(position, nearestMarkerPosition);

    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsArea, getLatLngBoundsPadding(0.1));
    List<MapCameraUpdate> cameraUpdates = new ArrayList<>();

    if (animate) {
      cameraUpdates.add(new MapCameraUpdate(cameraUpdate, true, 1000));
    } else {
      cameraUpdates.add(new MapCameraUpdate(cameraUpdate, false, 0));
    }

    cameraUpdates.add(new MapCameraUpdate(CameraUpdateFactory.newLatLng(position), true, 10));

    MapCameraUpdates mapCameraUpdates = new MapCameraUpdates(googleMap, cameraUpdates);

    Property<MapCameraUpdates, Integer> property = Property.of(MapCameraUpdates.class, Integer.class, "index");
    ObjectAnimator animator = ObjectAnimator.ofInt(mapCameraUpdates, property, 0, cameraUpdates.size() - 1);

    animator.setDuration(mapCameraUpdates.getTotalDuration());
    animator.start();
  }

  private int getLatLngBoundsPadding(double scale) {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    return (int)Math.max(displayMetrics.widthPixels * scale, displayMetrics.heightPixels * scale);
  }

  private double getPositionsDistance(LatLng from, LatLng to) {
    return SphericalUtil.computeDistanceBetween(from, to);

//    double chLatitude = to.latitude - from.latitude;
//    double chLongitude = to.longitude - from.longitude;
//    double dLatitude = chLatitude * (Math.PI / 180);
//    double dLongitude = chLongitude * (Math.PI / 180);
//    double rLatitude1 = to.latitude * (Math.PI / 180);
//    double rLatitude2 = to.latitude * (Math.PI / 180);
//    double a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2) + Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2) * Math.cos(rLatitude1) * Math.cos(rLatitude2);
//
//    return 12742000 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private LatLng findNearestMarkerPosition(LatLng currentPosition, List<Marker> markers, List<LatLng> omitPositions) {
    List<PositionDistance> positionDistances = new ArrayList<>();

    for (Marker marker : markers) {
      LatLng position = marker.getPosition();

      if (omitPositions.indexOf(position) < 0) {
        positionDistances.add(new PositionDistance(currentPosition, marker));
      }
    }

    PositionDistance result = null;

    for (PositionDistance positionDistance : positionDistances) {
      if (result == null || positionDistance.getDistance() < result.getDistance()) {
        result = positionDistance;
      }
    }

    if (result != null) {
      return result.getPosition();
    } else {
      return currentPosition;
    }
  }

  private static class MapCameraUpdate {
    private CameraUpdate update;
    private boolean animate = false;
    private boolean done = false;
    private int duration = 0;

    MapCameraUpdate (CameraUpdate update, boolean animate, int duration) {
      this.update = update;
      this.animate = animate;
      this.duration = duration;
    }

    int getDuration() {
      return duration;
    }

    void run(GoogleMap googleMap) {
      if (!done) {
        done = true;

        try {
          if (animate) {
            googleMap.animateCamera(update, duration, null);
          } else {
            googleMap.moveCamera(update);
          }
        } catch (IllegalStateException exception) {
          Log.e(TAG, exception.toString());
        }
      }
    }
  }

  private static class MapCameraUpdates {
    private GoogleMap googleMap;
    private List<MapCameraUpdate> updates;
    private int index = 0;

    MapCameraUpdates (GoogleMap googleMap, List<MapCameraUpdate> updates) {
      this.googleMap = googleMap;
      this.updates = updates;
    }

    void setIndex(int index) {
      this.index = index;
      updates.get(index).run(googleMap);
    }

    int getIndex() {
      return index;
    }

    int getTotalDuration() {
      int duration = 0;

      for (MapCameraUpdate update : updates) {
        duration += update.getDuration();
      }

      return duration;
    }
  }

  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      return SphericalUtil.interpolate(startValue, endValue, fraction);
    }
  }

  private class PositionDistance {
    private LatLng position;
    private double distance;

    PositionDistance (LatLng from, Marker to) {
      this.distance = getPositionsDistance(from, to.getPosition());
      this.position = to.getPosition();
    }

    LatLng getPosition() {
      return position;
    }

    double getDistance() {
      return distance;
    }
  }

  private LatLngBounds createBoundsArea(LatLng position, LatLng nearestPosition) {
    double distance = getPositionsDistance(position, nearestPosition);

    return new LatLngBounds(SphericalUtil.computeOffset(position, distance, 180), SphericalUtil.computeOffset(position, distance, 0));
  }
}
