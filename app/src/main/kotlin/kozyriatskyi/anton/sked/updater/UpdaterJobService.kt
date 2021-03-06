package kozyriatskyi.anton.sked.updater

import android.annotation.SuppressLint
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
import com.crashlytics.android.Crashlytics
import com.firebase.jobdispatcher.*
import kozyriatskyi.anton.sked.R
import kozyriatskyi.anton.sked.data.repository.UserInfoStorage
import kozyriatskyi.anton.sked.data.repository.UserSettingsStorage
import kozyriatskyi.anton.sked.di.Injector
import kozyriatskyi.anton.sked.main.MainActivity
import kozyriatskyi.anton.sked.repository.ScheduleProvider
import kozyriatskyi.anton.sked.util.ScheduleUpdateTimeLogger
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

private fun startTime(calendar: Calendar): Int {
    val startTime = calendar.let {
        it.set(Calendar.HOUR_OF_DAY, START_HOUR)
        it.set(Calendar.MINUTE, (random() * 60).toInt())
        it.set(Calendar.SECOND, 0)

        it.timeInMillis
    }

    val todayIsTooLate = System.currentTimeMillis() >= startTime

    if (todayIsTooLate) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    val millisFromNow = calendar.timeInMillis - System.currentTimeMillis()

    return TimeUnit.MILLISECONDS.toSeconds(millisFromNow).toInt()
}

private fun endTime(calendar: Calendar): Int {
    calendar.set(Calendar.HOUR_OF_DAY, END_HOUR)
    calendar.set(Calendar.MINUTE, 0)

    val millisFromNow = calendar.timeInMillis - System.currentTimeMillis()

    return TimeUnit.MILLISECONDS.toSeconds(millisFromNow).toInt()
}


class UpdaterJobService : JobService() {

    companion object {
        private const val TAG = "UpdaterJobService"

        private const val OLD_CHANNEL_ID = "updater_01"
        private const val CHANNEL_ID = "updater_02"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

            val calendar = Calendar.getInstance()

            val startTime = startTime(calendar)
            val endTime = endTime(calendar)

            val job = dispatcher.newJobBuilder()
                    .setService(UpdaterJobService::class.java)
                    .setTag(TAG)
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
//                    .setTrigger(Trigger.executionWindow(30, 60))
                    .setTrigger(Trigger.executionWindow(startTime, endTime))
                    .setReplaceCurrent(true)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .build()

            try {
                dispatcher.mustSchedule(job)
            } catch (e: FirebaseJobDispatcher.ScheduleFailedException) {
                Crashlytics.logException(e)
            }
        }
    }

    @Inject
    lateinit var scheduleLoader: ScheduleProvider

    @Inject
    lateinit var userPreferences: UserSettingsStorage

    @Inject
    lateinit var userInfoPreferences: UserInfoStorage

    @Inject
    lateinit var timeLogger: ScheduleUpdateTimeLogger

    override fun onCreate() {
        Injector.inject(this)
    }

    override fun onStartJob(job: JobParameters): Boolean {
        Thread {
            try {
                update(job)
            } catch (ignore: Exception) {
            }
        }.start()

        return true
    }

    override fun onStopJob(job: JobParameters): Boolean = true

    private fun update(job: JobParameters): Boolean {
        var isSuccessfullyUpdated = true

        try {
            val user = userInfoPreferences.getUser()
            scheduleLoader.getSchedule(user)
            timeLogger.saveTime()
        } catch (t: Throwable) {
            isSuccessfullyUpdated = false
            logE("Error updating schedule: ${t.message}", t)
            Crashlytics.logException(t)
        }

        val notifyOnUpdate = userPreferences.getBoolean(UserSettingsStorage.KEY_NOTIFY_ON_UPDATE, true)

        if (notifyOnUpdate) sendNotification(isSuccessfullyUpdated, applicationContext)

        start(applicationContext)

        jobFinished(job, isSuccessfullyUpdated.not())

        return isSuccessfullyUpdated
    }

    @SuppressLint("NewApi")
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
            val importance = NotificationManagerCompat.IMPORTANCE_MIN

            val channel = NotificationChannel(CHANNEL_ID, chName, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.GREEN

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(OLD_CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_schedule_updated_title))
                .setContentText(context.getString(contentTextId))
                .setSmallIcon(R.drawable.ic_notif_update)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }
}