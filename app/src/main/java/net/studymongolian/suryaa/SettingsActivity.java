package net.studymongolian.suryaa;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    protected static final String KEY_PREF_CURRENT_LIST = "current_list";
    static final String KEY_PREF_NIGHT_MODE = "pref_night_mode"; // also in preferences.xml
    static final String KEY_PREF_FONT = "pref_font"; // also in preferences.xml
    static final String KEY_PREF_FONT_DEFAULT = "printing"; // also in preferences.xml
    static final String KEY_PREF_HELP = "pref_help"; // also in preferences.xml
    static final String KEY_PREF_ABOUT = "pref_about"; // also in preferences.xml

    public static final String QIMED = "fonts/MQD8102.ttf";         // Handwriting font

    boolean mOldNightModeSetting;
    String mOldFontSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mOldNightModeSetting = getIntent().getBooleanExtra(KEY_PREF_NIGHT_MODE, false);
        mOldFontSetting = getIntent().getStringExtra(KEY_PREF_FONT);
        if (mOldFontSetting == null) mOldFontSetting = KEY_PREF_FONT_DEFAULT;

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            SettingsFragment settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, settingsFragment).commit();
        }
    }

    public static void setNightMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isNightMode = sharedPref.getBoolean(KEY_PREF_NIGHT_MODE, false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean currentNightModeSetting = sharedPref.getBoolean(KEY_PREF_NIGHT_MODE, false);
                String currentFontSetting = sharedPref.getString(KEY_PREF_FONT, KEY_PREF_FONT_DEFAULT);
                if (mOldNightModeSetting != currentNightModeSetting
                        || !mOldFontSetting.equals(currentFontSetting)) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            setAboutClickListener();
            setHelpClickListener();
        }

        private void setAboutClickListener() {
            Preference pref = getPreferenceManager().findPreference(KEY_PREF_ABOUT);
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (preference.getKey().equals(KEY_PREF_ABOUT)) {
                            Intent intent = new Intent(getActivity(), AboutActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        private void setHelpClickListener() {
            Preference pref = getPreferenceManager().findPreference(KEY_PREF_HELP);
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (preference.getKey().equals(KEY_PREF_HELP)) {
                            Intent intent = new Intent(getActivity(), HelpActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_NIGHT_MODE)) {
                getActivity().recreate();
            }
        }
    }
}