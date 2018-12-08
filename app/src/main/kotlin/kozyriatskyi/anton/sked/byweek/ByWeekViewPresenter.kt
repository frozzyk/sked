package kozyriatskyi.anton.sked.byweek

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kozyriatskyi.anton.sked.util.DateUtils
import java.util.*

@InjectViewState
class ByWeekViewPresenter(private val interactor: ByWeekViewInteractor) : MvpPresenter<ByWeekView>() {

    private val disposables = CompositeDisposable()

    override fun onFirstViewAttach() {
        subscribe()
    }

    private fun subscribe() {
        thisWeekendLessonsCount()
    }

    private fun thisWeekendLessonsCount() {
        val disposable = interactor.firstWeekendLessonsCount()
                .map { Pair(it.first != 0, it.second != 0) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (hasLessonsOnSaturday, hasLessonsOnSunday) ->
                    val shouldShowNextWeek = shouldShowNextWeek(hasLessonsOnSaturday, hasLessonsOnSunday)
                    viewState.showWeeks(getDateTitles())
                    if (shouldShowNextWeek) viewState.showNextWeek()
                }

        disposables.add(disposable)
    }

    private fun shouldShowNextWeek(hasLessonsOnSaturday: Boolean, hasLessonsOnSunday: Boolean): Boolean {
        val c = Calendar.getInstance()

        val dayOfWeek = c[Calendar.DAY_OF_WEEK]

        if (dayOfWeek == Calendar.SUNDAY) {
            return hasLessonsOnSunday.not()
        }

        val isWeekendToday = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        val hasLessonsOnWeekend = hasLessonsOnSaturday or hasLessonsOnSunday
        // if today is weekend and there are no lessons on Saturday or Sunday - show next week
        return isWeekendToday and hasLessonsOnWeekend.not()
    }

    private fun getDateTitles(): Array<String> {
        return Array(6) {
            "${DateUtils.mondayDate(it - 1, inShortFormat = true)} - ${DateUtils.sundayDate(it - 1, inShortFormat = true)}"
        }
    }

    override fun onDestroy() {
        disposables.dispose()
    }
}