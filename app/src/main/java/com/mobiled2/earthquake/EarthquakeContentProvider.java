package com.mobiled2.earthquake;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

public class EarthquakeContentProvider extends ContentProvider {
  public static final Uri CONTENT_URI = Uri.parse("content://com.mobiled2.earthquakecontentprovider");

  public static final String KEY_ID = "_id";
  public static final String KEY_DATE = "date";
  public static final String KEY_DETAILS = "details";
  public static final String KEY_SUMMARY = "summary";
  public static final String KEY_LOCATION_LATITUDE = "latitude";
  public static final String KEY_LOCATION_LONGITUDE = "longitude";
  public static final String KEY_MAGNITUDE = "magnitude";

  private EarthquakeDatabaseHelper dbHelper;

  private static final int QUAKES = 1;
  private static final int QUAKE_ID = 2;

  private static final UriMatcher uriMatcher;

  private Context context;

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    uriMatcher.addURI("com.mobiled2.earthquakecontentprovider", "earthquakes", QUAKES);
    uriMatcher.addURI("com.mobiled2.earthquakecontentprovider", "earthquakes/#", QUAKE_ID);
  };

  @Override
  public boolean onCreate() {
    context = getContext();
    dbHelper = new EarthquakeDatabaseHelper(context, EarthquakeDatabaseHelper.DATABASE_NAME, null, EarthquakeDatabaseHelper.DATABASE_VERSION);
    return true;
  }

  @Override
  public String getType(@NonNull Uri uri) {
    switch (uriMatcher.match(uri)) {
      case QUAKES: return "vnd.android.cursor.dir/vnd.mobiled2.earthquake";
      case QUAKE_ID: return "vnd.android.cursor.item/vnd.mobiled2.earthquake";
      default: throw new IllegalArgumentException("Unsupported URI:" + uri);
    }
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    SQLiteDatabase database = dbHelper.getWritableDatabase();
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    qb.setTables(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE);

    switch (uriMatcher.match(uri)) {
      case QUAKE_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
      default: break;
    }

    if (TextUtils.isEmpty(sortOrder)) {
      sortOrder = KEY_DATE;
    }

    Cursor cursor = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);

    cursor.setNotificationUri(context.getContentResolver(), uri);

    return cursor;
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    SQLiteDatabase database = dbHelper.getWritableDatabase();
    long rowId = database.insert(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, "quake", values);

    if (rowId > 0) {
      Uri rowUri = ContentUris.withAppendedId(CONTENT_URI, rowId);

      context.getContentResolver().notifyChange(uri, null);

      return rowUri;
    }

    throw new SQLException("Failed to insert row into " + uri);
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    SQLiteDatabase database = dbHelper.getWritableDatabase();
    int count = 0;

    switch (uriMatcher.match(uri)) {
      case QUAKES:
        count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, values, selection, selectionArgs);
      break;
      case QUAKE_ID:
        count = database.update(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, values, KEY_ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
      break;
      default: throw new IllegalArgumentException("Unsupported URI:" + uri);
    }

    return count;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    SQLiteDatabase database = dbHelper.getWritableDatabase();
    int count = 0;

    switch (uriMatcher.match(uri)) {
      case QUAKES:
        count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, selection, selectionArgs);
      break;
      case QUAKE_ID:
        count = database.delete(EarthquakeDatabaseHelper.EARTHQUAKE_TABLE, KEY_ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
      break;
      default: throw new IllegalArgumentException("Unsupported URI:" + uri);
    }

    context.getContentResolver().notifyChange(uri, null);

    return count;
  }

  private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "earthquakes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String EARTHQUAKE_TABLE = "earthquakes";

    private static final String TAG = "EARTHQUAKE_PROVIDER";

    private SQLiteDatabase earthquakesDb;

    EarthquakeDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
      super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(
        "CREATE TABLE " + EARTHQUAKE_TABLE +
        " (" +
          KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
          KEY_DATE + " INTEGER, " +
          KEY_DETAILS + " TEXT, " +
          KEY_SUMMARY + " TEXT, " +
          KEY_LOCATION_LATITUDE + " FLOAT, " +
          KEY_LOCATION_LONGITUDE + " FLOAT, " +
          KEY_MAGNITUDE + " FLOAT" +
        ");"
      );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to version " + newVersion);

      db.execSQL("DROP TABLE IF EXISTS " + EARTHQUAKE_TABLE);

      onCreate(db);
    }
  };
}
