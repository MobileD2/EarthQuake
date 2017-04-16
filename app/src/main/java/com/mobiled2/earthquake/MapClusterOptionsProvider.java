package com.mobiled2.earthquake;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

class MapClusterOptionsProvider implements ClusterOptionsProvider {
  private final LruCache<Pair<Integer, Integer>, BitmapDescriptor> cache = new LruCache<>(128);
  private final ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);
  private float startValue;
  private float endValue;
  private float startHue;
  private float endHue;
  private String clusteringColor;
  private final Paint circlePaint;
  private final Paint circleShadowPaint;
  private final Paint textPaint;
  private final Rect bounds = new Rect();
  private float blurRadius;
  private float textPadding;
  private float shadowOffsetX;
  private float shadowOffsetY;
  private Context context;

  MapClusterOptionsProvider(Context context, Resources resources, float startValue, float endValue, float startHue, float endHue, String clusteringColor) {
    this.context = context;
    this.startValue = startValue;
    this.endValue = endValue;
    this.startHue = startHue;
    this.endHue = endHue;
    this.clusteringColor = clusteringColor;
    circlePaint = createCirclePaint(resources);
    circleShadowPaint = createCircleShadowPaint(resources);
    textPaint = createTextPaint(resources);
    textPadding = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_text_padding);
  }

  private Paint createCirclePaint(Resources resources) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    blurRadius = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_circle_blur_radius);
    if (blurRadius > 0.0f) {
      BlurMaskFilter maskFilter = new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID);
      paint.setMaskFilter(maskFilter);
    }
    return paint;
  }

  private Paint createCircleShadowPaint(Resources resources) {
    Paint paint = null;
    float circleShadowBlurRadius = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_circle_shadow_blur_radius);
    if (circleShadowBlurRadius > 0.0f) {
      paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      float offsetX = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_circle_shadow_offset_x);
      float offsetY = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_circle_shadow_offset_y);
      int color = ContextCompat.getColor(context, com.androidmapsextensions.R.color.ame_default_cluster_circle_shadow_color);
      paint.setShadowLayer(circleShadowBlurRadius, offsetX, offsetY, color);
    }
    return paint;
  }

  private Paint createTextPaint(Resources resources) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(ContextCompat.getColor(context, com.androidmapsextensions.R.color.ame_default_cluster_text_color));
    float shadowBlurRadius = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_text_shadow_blur_radius);
    if (shadowBlurRadius > 0.0f) {
      shadowOffsetX = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_text_shadow_offset_x);
      shadowOffsetY = resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_text_shadow_offset_y);
      int shadowColor = ContextCompat.getColor(context, com.androidmapsextensions.R.color.ame_default_cluster_text_shadow_color);
      paint.setShadowLayer(shadowBlurRadius, shadowOffsetX, shadowOffsetY, shadowColor);
    }
    paint.setTextSize(resources.getDimension(com.androidmapsextensions.R.dimen.ame_default_cluster_text_size));
    paint.setTypeface(Typeface.DEFAULT_BOLD);
    return paint;
  }

  private float calculateMinValue(List<Marker> markers) {
    float result = 100;

    for (Marker marker : markers) {
      float value = marker.getData();

      if (value < result) {
        result = value;
      }
    }

    return result;
  }

  private float calculateMeanValue(List<Marker> markers) {
    float sum = 0;

    for (Marker marker : markers) {
      float value = marker.getData();

      sum += value;
    }

    return sum / markers.size();
  }

  private float calculateMaxValue(List<Marker> markers) {
    float result = 0;

    for (Marker marker : markers) {
      float value = marker.getData();

      if (value > result) {
        result = value;
      }
    }

    return result;
  }

  private float detectClusteringColor(float minValue, float meanValue, float maxValue) {
    if (clusteringColor.equals("Min")) {
      return minValue;
    } else if (clusteringColor.equals("Max")) {
      return maxValue;
    }

    return meanValue;
  }

  @Override
  public ClusterOptions getClusterOptions(List<Marker> markers) {
    float minValue = calculateMinValue(markers);
    float meanValue = calculateMeanValue(markers);
    float maxValue = calculateMaxValue(markers);
    int count = markers.size();

    Pair<Integer, Integer> cacheKey = Pair.create((int)(detectClusteringColor(minValue, meanValue, maxValue) * 1E2), count);
    BitmapDescriptor icon = cache.get(cacheKey);

    if (icon == null) {
      icon = createIcon(minValue, meanValue, maxValue, count);
      cache.put(cacheKey, icon);
    }

    clusterOptions.icon(icon);
    return clusterOptions;
  }

  private BitmapDescriptor createIcon(float minValue, float meanValue, float maxValue, int count) {
    String text = String.valueOf(count);
    calculateTextSize(text);
    int iconSize = calculateIconSize();
    Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawCircle(canvas, minValue, meanValue, maxValue, count, iconSize);
    drawText(canvas, text, iconSize);
    return BitmapDescriptorFactory.fromBitmap(bitmap);
  }

  private void calculateTextSize(String text) {
    textPaint.getTextBounds(text, 0, text.length(), bounds);
  }

  private int calculateIconSize() {
    int w = bounds.width();
    int h = bounds.height();
    return (int) Math.ceil(2 * (textPadding + blurRadius) + Math.sqrt(w * w + h * h));
  }

  private void drawCircle(Canvas canvas, float minValue, float meanValue, float maxValue, int count, float iconSize) {
    canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - blurRadius, circleShadowPaint);

    float value = detectClusteringColor(minValue, meanValue, maxValue);
    float hue = (((value - endValue) * startHue - (value - startValue) * endHue) / (startValue - endValue));
    circlePaint.setColor(ColorUtils.HSLToColor(new float[] { hue, 0.75f, 0.5f }));

    canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - blurRadius, circlePaint);
  }

  private void drawText(Canvas canvas, String text, int iconSize) {
    int x = Math.round((iconSize - bounds.width()) / 2 - bounds.left - shadowOffsetX / 2);
    int y = Math.round((iconSize - bounds.height()) / 2 - bounds.top - shadowOffsetY / 2);
    canvas.drawText(text, x, y, textPaint);
  }
}
