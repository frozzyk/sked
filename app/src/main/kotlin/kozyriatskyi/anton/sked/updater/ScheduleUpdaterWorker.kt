package kozyriatskyi.anton.sked.updater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.crashlytics.android.Crashlytics
import kozyriatskyi.anton.sked.R
import kozyriatskyi.anton.sked.data.pojo.LessonMapper
import kozyriatskyi.anton.sked.data.repository.UserInfoStorage
import kozyriatskyi.anton.sked.data.repository.UserSettingsStorage
import kozyriatskyi.anton.sked.di.Injector
import kozyriatskyi.anton.sked.main.MainActivity
import kozyriatskyi.anton.sked.repository.ScheduleProvider
import kozyriatskyi.anton.sked.repository.ScheduleStorage
import kozyriatskyi.anton.sked.util.ScheduleUpdateTimeLogger
import kozyriatskyi.anton.sked.util.logD
import kozyriatskyi.anton.sked.util.logE
import java.lang.Math.random
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Created by Anton on 06.09.2017.
 */

private const val START_HOUR = 18
private const val END_HOUR = 20
private const val BACKOFF_DELAY_MINS = 1L

private fun calculateStartTimeDelay(): Long {
    val calendar = Calendar.getInstance()

    val startTime = calendar.let {
        it.set(Calendar.HOUR_OF_DAY, START_HOUR)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)

        it.timeInMillis
    }

    val todayIsTooLate = System.currentTimeMillis() >= startTime

    if (todayIsTooLate) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    val minsToAdd = (random() * ((END_HOUR - START_HOUR) * 60)).toInt()
    calendar.add(Calendar.MINUTE, minsToAdd)

    return calendar.timeInMillis - System.currentTimeMillis()
}

class ScheduleUpdaterWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        private const val WORK_TAG = "ScheduleUpdaterWorker:job"

        private const val NOTIFICATION_CHANNEL_ID = "updater_02"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val initialDelayMillis = calculateStartTimeDelay()

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                    .build()

            val request = OneTimeWorkRequest.Builder(ScheduleUpdaterWorker::class.java)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_DELAY_MINS, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .build()

            try {
                val operation = WorkManager.getInstance(context)
                        .enqueueUniqueWork(
                                WORK_TAG,
                                ExistingWorkPolicy.KEEP,
                                request
                        )
                operation.result.get()
                logD("Updater work successfully scheduled")
            } catch (e: Exception) {
                Crashlytics.logException(e)
                logD("Failed to schedule updater work: ${e.message}")
            }
        }
    }

    @Inject
    lateinit var scheduleLoader: ScheduleProvider

    @Inject
    lateinit var scheduleStorage: ScheduleStorage

    @Inject
    lateinit var lessonsMapper: LessonMapper

    @Inject
    lateinit var userSettingsStorage: UserSettingsStorage

    @Inject
    lateinit var userInfoPreferences: UserInfoStorage

    @Inject
    lateinit var timeLogger: ScheduleUpdateTimeLogger

    override fun doWork(): Result {
        //TODO move injection into WorkManagerFactory
        Injector.inject(this)

        return updateSchedule().also { result -> rescheduleWorkIfNeeded(result) }
    }

    private fun updateSchedule(): Result {
        var isSuccessfullyUpdated = true

        try {
            val user = userInfoPreferences.getUser()
            val schedule = scheduleLoader.getSchedule(user)
            val dbSchedule = lessonsMapper.networkToDb(schedule)

            scheduleStorage.saveLessons(dbSchedule)
            timeLogger.saveTime()
        } catch (t: Throwable) {
            isSuccessfullyUpdated = false

            logE("Error updating schedule: ${t.message}", t)
            Crashlytics.logException(t)
        }

        val notifyOnUpdate = userSettingsStorage.getBoolean(UserSettingsStorage.KEY_NOTIFY_ON_UPDATE, true)

        if (notifyOnUpdate) {
            sendNotification(isSuccessfullyUpdated, applicationContext)
        }

        return if (isSuccessfullyUpdated) Result.success() else Result.retry()
    }

    private fun sendNotification(successfullyUpdated: Boolean, context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManagerCompat = NotificationManagerCompat.from(context)

        val contentTextId = if (successfullyUpdated) R.string.notification_schedule_updated_successfully
        else R.string.notification_schedule_updated_unsuccessfully

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chName = "Sked channel"
            val description = "Channel for all notifications"
            val importance = NotificationManager.IMPORTANCE_MIN

            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, chName, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.GREEN

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_schedule_updated_title))
                .setContentText(context.getString(contentTextId))
                .setSmallIcon(R.drawable.ic_notif_update)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }

    private fun rescheduleWorkIfNeeded(result: Result) {
        if (result is Result.Success) {
            start(applicationContext)
        }
    }
}