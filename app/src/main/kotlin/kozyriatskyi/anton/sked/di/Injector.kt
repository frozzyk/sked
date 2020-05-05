package kozyriatskyi.anton.sked.di

import android.annotation.SuppressLint
import android.content.Context
import kozyriatskyi.anton.sked.audiences.AudiencesActivity
import kozyriatskyi.anton.sked.byday.ByDayViewFragment
import kozyriatskyi.anton.sked.byday.DaggerByDayViewComponent
import kozyriatskyi.anton.sked.byweek.ByWeekViewFragment
import kozyriatskyi.anton.sked.byweek.DaggerByWeekViewComponent
import kozyriatskyi.anton.sked.day.DaggerDayViewComponent
import kozyriatskyi.anton.sked.day.DayViewFragment
import kozyriatskyi.anton.sked.day.DayViewModule
import kozyriatskyi.anton.sked.di.module.AppModule
import kozyriatskyi.anton.sked.login.DaggerLoginComponent
import kozyriatskyi.anton.sked.login.LoginActivity
import kozyriatskyi.anton.sked.login.LoginModule
import kozyriatskyi.anton.sked.login.LoginView
import kozyriatskyi.anton.sked.login.student.DaggerStudentLoginComponent
import kozyriatskyi.anton.sked.login.student.StudentLoginFragment
import kozyriatskyi.anton.sked.login.teacher.DaggerTeacherLoginComponent
import kozyriatskyi.anton.sked.login.teacher.TeacherLoginFragment
import kozyriatskyi.anton.sked.main.DaggerMainComponent
import kozyriatskyi.anton.sked.main.MainActivity
import kozyriatskyi.anton.sked.repository.AudiencesComponent
import kozyriatskyi.anton.sked.repository.DaggerAudiencesComponent
import kozyriatskyi.anton.sked.settings.DaggerSettingsComponent
import kozyriatskyi.anton.sked.settings.SettingsFragment
import kozyriatskyi.anton.sked.updater.DaggerUpdaterComponent
import kozyriatskyi.anton.sked.updater.ScheduleUpdaterWorker
import kozyriatskyi.anton.sked.week.DaggerWeekViewComponent
import kozyriatskyi.anton.sked.week.WeekViewFragment
import kozyriatskyi.anton.sked.week.WeekViewModule

/**
 * Created by Anton on 02.02.2018.
 */
@SuppressLint("StaticFieldLeak")
object Injector {

    private lateinit var appContext: Context

    private val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(appContext))
                .build()
    }

    private var audiencesComponent: AudiencesComponent? = null
        get() {
            if (field == null) {
                field = DaggerAudiencesComponent.builder()
                        .appComponent(appComponent)
                        .build()
            }
            return field
        }

    fun init(appContext: Context) {
        this.appContext = appContext
    }

    fun inject(activity: MainActivity) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(activity)
    }

    fun inject(fragment: StudentLoginFragment) {
        DaggerStudentLoginComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(fragment: TeacherLoginFragment) {
        DaggerTeacherLoginComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(fragment: DayViewFragment, dayNumber: Int, nextWeek: Boolean) {
        DaggerDayViewComponent.builder()
                .dayViewModule(DayViewModule(dayNumber, nextWeek))
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(fragment: WeekViewFragment, weekNumber: Int) {
        DaggerWeekViewComponent.builder()
                .weekViewModule(WeekViewModule(weekNumber))
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(fragment: ByDayViewFragment) {
        DaggerByDayViewComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(fragment: ByWeekViewFragment) {
        DaggerByWeekViewComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(serviceSchedule: ScheduleUpdaterWorker) {
        DaggerUpdaterComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(serviceSchedule)
    }

    fun inject(fragment: SettingsFragment) {
        DaggerSettingsComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(fragment)
    }

    fun inject(activity: AudiencesActivity) {
        requireNotNull(audiencesComponent).inject(activity)
    }

    fun release(activity: AudiencesActivity) {
        audiencesComponent = null
    }

    fun inject(activity: LoginActivity, userType: LoginView.UserType) {
        DaggerLoginComponent.builder()
                .appComponent(appComponent)
                .loginModule(LoginModule(userType))
                .build()
                .inject(activity)
    }
}