package kozyriatskyi.anton.sked

import android.app.Application
import android.content.Context
import kozyriatskyi.anton.sked.util.edit

@Suppress("SameParameterValue")
abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        checkAppVersion()
    }

    protected abstract fun onApplicationUpdate(previousVersionName: String, previousVersionCode: Int,
                                           currentVersionName: String, currentVersionCode: Int)

    private fun checkAppVersion() {
        val preferences = getSharedPreferences("versions", Context.MODE_PRIVATE)

        val currentVersionCode = BuildConfig.VERSION_CODE
        val previousVersionCode = preferences.getInt("version_code", currentVersionCode - 1)

        if (previousVersionCode != currentVersionCode) {
            val currentVersionName = BuildConfig.VERSION_NAME
            val previousVersionName = preferences.getString("version_name", "$currentVersionName old")!!

            preferences.edit {
                putString("version_name", currentVersionName)
                putInt("version_code", currentVersionCode)
            }

            onApplicationUpdate(previousVersionName, previousVersionCode,
                    currentVersionName, currentVersionCode)
        }
    }
}