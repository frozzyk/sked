package kozyriatskyi.anton.sked.util

import android.content.Context
import kozyriatskyi.anton.sked.updater.ScheduleUpdaterWorker

class JobManager(private val context: Context) {

    fun launchUpdaterJob() {
        ScheduleUpdaterWorker.start(context)
    }
}