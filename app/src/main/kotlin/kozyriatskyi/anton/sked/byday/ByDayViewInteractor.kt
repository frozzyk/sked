package kozyriatskyi.anton.sked.byday

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kozyriatskyi.anton.sked.repository.ScheduleStorage
import kozyriatskyi.anton.sked.util.DateUtils

class ByDayViewInteractor(private val scheduleStorage: ScheduleStorage) {

    fun thisWeekendLessonsCount(): Observable<Pair<Boolean, Boolean>> {
        val thisSat = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.saturdayDate())
        val thisSun = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.sundayDate())

        return Observable.zip(thisSat, thisSun, BiFunction { t1: Int, t2: Int -> Pair(t1, t2) })
                .map { Pair(it.first != 0, it.second != 0) }
    }

    fun weekendLessonsCount(nextWeek: Boolean): Observable<Pair<Boolean, Boolean>> {
        val weekNum = if (nextWeek) 1 else 0

        val sat = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.saturdayDate(weekNum))
        val sun = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.sundayDate(weekNum))

        return Observable.zip(sat, sun, BiFunction { t1: Int, t2: Int -> Pair(t1, t2) })
                .map { Pair(it.first != 0, it.second != 0) }
    }
}