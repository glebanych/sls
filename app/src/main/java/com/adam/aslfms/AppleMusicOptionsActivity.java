package com.adam.aslfms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.adam.aslfms.service.applemusic.NotificationService;
import com.adam.aslfms.util.AppSettings;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

/**
 * Created by 4-Eyes on 15/3/2017.
 *
 */

public class AppleMusicOptionsActivity extends AppCompatPreferenceActivity {

    CheckBoxPreference cbp;
    AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.apple_music_options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        cbp = (CheckBoxPreference) findPreference("apple_notification_listening");
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            cbp.setEnabled(false);
            cbp.setSummary("Unfortunately your current version of android does not support this feature");
        }
        settings = new AppSettings(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cbp.setOnPreferenceClickListener(handleClick);
        cbp.setChecked(settings.getAppleListenerEnabled());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Preference.OnPreferenceClickListener handleClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!cbp.isEnabled()) return true;
            final Intent intent = new Intent(AppleMusicOptionsActivity.this, NotificationService.class);
            if (cbp.isChecked()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppleMusicOptionsActivity.this);
                builder.setMessage("Enabling this feature will mean Simple Last.fm Scrobbler will be able to listen to your notifications. Are you sure you want to proceed?")
                .setTitle("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.setAppleListenerEnabled(true);
                        cbp.setChecked(true);
                        // Start notification service
                        startService(intent);
                        // Redirect to settings for enabling notification listening
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                            Intent settingsIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            startActivityForResult(settingsIntent, 0);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Disable checkbox user cancelled action.
                        cbp.setChecked(false);
                    }
                });

                builder.create().show();
            } else {
                // Update setting and stop service.
                settings.setAppleListenerEnabled(false);
                stopService(intent);
            }
            return true;
        }
    };
}
