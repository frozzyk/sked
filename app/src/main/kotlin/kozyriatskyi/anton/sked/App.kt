package kozyriatskyi.anton.sked

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import kozyriatskyi.anton.sked.data.pojo.Student
import kozyriatskyi.anton.sked.data.repository.UserInfoStorage
import kozyriatskyi.anton.sked.data.repository.UserSettingsStorage
import kozyriatskyi.anton.sked.di.Injector
import kozyriatskyi.anton.sked.di.module.StorageModule
import kozyriatskyi.anton.sked.updater.ScheduleUpdaterWorker
import kozyriatskyi.anton.sked.util.logD


class App : BaseApplication() {

    override fun onCreate() {
        Injector.init(this)
        super.onCreate()

        val preferences = UserSettingsStorage(PreferenceManager.getDefaultSharedPreferences(this))

        applyTheme(preferences)

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false)
    }


    private fun applyTheme(sharedPreferences: UserSettingsStorage) {
        val defaultTheme = sharedPreferences.getString(UserSettingsStorage.KEY_DEFAULT_THEME,
                "0").toInt()
        AppCompatDelegate.setDefaultNightMode(defaultTheme)
    }

    override fun onApplicationUpdate(previousVersionName: String, previousVersionCode: Int,
                                     currentVersionName: String, currentVersionCode: Int) {
        relaunchUpdaterJob()

        val preferences = getSharedPreferences(StorageModule.PREFERENCES_USER_INFO, Context.MODE_PRIVATE)
        val userInfoStorage = UserInfoStorage(preferences)

        try {
            val user = userInfoStorage.getUser()

            this.logD("USER_TYPE: $user")
            val type = if (user is Student) "student" else "teacher"
            FirebaseAnalytics.getInstance(this)
                    .setUserProperty("user_type", type)
        } catch (ignore: IllegalStateException) {
            //no user saved - app is launched for the first time
        }
    }

    private fun relaunchUpdaterJob() = ScheduleUpdaterWorker.start(this)
}