package com.mobiled2.earthquake;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class PreferencesActivity extends AppCompatActivity {
  public static final String USER_PREFERENCE = "USER_PREFERENCE";
  public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
  public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
  public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

  CheckBox autoUpdate;
  Spinner updateFreqSpinner;
  Spinner magnitudeSpinner;
  SharedPreferences prefs;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.preferences);

    updateFreqSpinner = (Spinner)findViewById(R.id.spinner_update_freq);
    magnitudeSpinner = (Spinner)findViewById(R.id.spinner_quake_mag);
    autoUpdate = (CheckBox)findViewById(R.id.checkbox_auto_update);

    populateSpinners();

    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    updateUIFromPreferences();

    Button okButton = (Button)findViewById(R.id.okButton);

    okButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        savePreferences();

        PreferencesActivity.this.setResult(RESULT_OK);

        finish();
      }
    });

    Button cancelButton = (Button)findViewById(R.id.cancelButton);

    cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        PreferencesActivity.this.setResult(RESULT_CANCELED);

        finish();
      }
    });
  }

  private void populateSpinners() {
    ArrayAdapter<CharSequence> fAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options, android.R.layout.simple_spinner_item);

    fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    updateFreqSpinner.setAdapter(fAdapter);

    ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this, R.array.magnitude_options, android.R.layout.simple_spinner_item);

    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    magnitudeSpinner.setAdapter(mAdapter);
  }

  private void updateUIFromPreferences() {
    updateFreqSpinner.setSelection(prefs.getInt(PREF_UPDATE_FREQ_INDEX, 0));
    magnitudeSpinner.setSelection(prefs.getInt(PREF_MIN_MAG_INDEX, 0));
    autoUpdate.setChecked(prefs.getBoolean(PREF_AUTO_UPDATE, false));
  }

  private void savePreferences() {
    SharedPreferences.Editor editor = prefs.edit();

    editor.putBoolean(PREF_AUTO_UPDATE, autoUpdate.isChecked());
    editor.putInt(PREF_UPDATE_FREQ_INDEX, updateFreqSpinner.getSelectedItemPosition());
    editor.putInt(PREF_MIN_MAG_INDEX, magnitudeSpinner.getSelectedItemPosition());

    editor.apply();
  }
}
