package com.mobiled2.earthquake;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

class QuakeDataCursorAdapter extends SimpleCursorAdapter {
  QuakeDataCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
    super(context, layout, c, from, to, flags);
  }

  @Override
  public void setViewText(TextView view, String text) {
    if (view.getId() == R.id.date) {
      text = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Long.valueOf(text));
    }

    view.setText(text);
  }
}
