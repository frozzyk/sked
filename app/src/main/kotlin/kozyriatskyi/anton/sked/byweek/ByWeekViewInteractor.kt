package kozyriatskyi.anton.sked.byweek

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kozyriatskyi.anton.sked.repository.ScheduleStorage
import kozyriatskyi.anton.sked.util.DateUtils

class ByWeekViewInteractor(private val scheduleStorage: ScheduleStorage) {

    fun firstWeekendLessonsCount(): Single<Pair<Int, Int>> {
        val thisSat = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.saturdayDate())
                .take(1)
                .singleOrError()
        val thisSun = scheduleStorage.getAmountOfLessonsOnDate(DateUtils.sundayDate())
                .take(1)
                .singleOrError()

        return Single.zip(thisSat, thisSun, BiFunction { t1: Int, t2: Int -> Pair(t1, t2) })
                .map { Pair(it.first, it.second) }
    }
}