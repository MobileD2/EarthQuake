package com.mobiled2.earthquake;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.joda.time.LocalDateTime;

class QuakeDataCursorAdapter extends SimpleCursorAdapter {
  QuakeDataCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
    super(context, layout, c, from, to, flags);
  }

  @Override
  public void setViewText(TextView view, String text) {
    switch(view.getId()) {
      case R.id.date:
        text = new LocalDateTime(Long.valueOf(text)).toString("dd-MM-yyyy HH:mm");
      break;
    }

    view.setText(text);
  }
}
