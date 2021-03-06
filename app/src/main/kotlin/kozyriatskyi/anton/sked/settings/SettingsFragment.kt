package kozyriatskyi.anton.sked.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import kozyriatskyi.anton.sked.R
import kozyriatskyi.anton.sked.data.repository.UserSettingsStorage
import kozyriatskyi.anton.sked.di.Injector
import kozyriatskyi.anton.sked.util.ScheduleUpdateTimeLogger
import kozyriatskyi.anton.sked.util.setGone
import javax.inject.Inject

/**
 * Created by Anton on 26.08.2017.
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val TAG = "settfr"
    }

    private var previousThemeValue = 0

    @Inject
    lateinit var scheduleUpdateTimeLogger: ScheduleUpdateTimeLogger

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Injector.inject(this)
        addPreferencesFromResource(R.xml.app_preferences)

        val themePreference = findPreference<ListPreference>(UserSettingsStorage.KEY_DEFAULT_THEME)!!
        previousThemeValue = themePreference.value.toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeTitle = view.findViewById<TextView>(R.id.settings_tv_update_time_title)
        val timeValue = view.findViewById<TextView>(R.id.settings_tv_update_time)

        val time = scheduleUpdateTimeLogger.getTime()

        if (time == null) {
            timeTitle.setGone()
            timeValue.setGone()
        } else {
            timeValue.text = time
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = findPreference<ListPreference>(key)!!

        pref.setSummary(pref.entry)

        if (key == UserSettingsStorage.KEY_DEFAULT_THEME) {
            val intValue = pref.value.toInt()
            AppCompatDelegate.setDefaultNightMode(intValue)

            if (previousThemeValue != intValue) {
                previousThemeValue = intValue

                with(requireActivity()) {
                    window.setWindowAnimations(R.style.WindowTransition_Fade)
                    recreate()
                }
            }
        }
    }
}