package com.androidnanodegree.mlopez.spotifystreamer.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnanodegree.mlopez.spotifystreamer.R;

import java.util.Locale;

/**
 * Created by marioromano on 30/08/2015.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    public static final int ISO_CODE_PICKED = 551;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] isoCountryCodes = Locale.getISOCountries();

        addPreferencesFromResource(R.xml.preferences);
        ListPreference listIsoCode = (ListPreference) findPreference(getActivity().getString(R.string.sp_iso_code));
        listIsoCode.setEntries(isoCountryCodes);
        listIsoCode.setEntryValues(isoCountryCodes);
        listIsoCode.setOnPreferenceChangeListener(this);
        listIsoCode.setDefaultValue(Locale.getDefault().getCountry());
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        getActivity().setResult(ISO_CODE_PICKED);
        getActivity().finish();
        return true;
    }
}
