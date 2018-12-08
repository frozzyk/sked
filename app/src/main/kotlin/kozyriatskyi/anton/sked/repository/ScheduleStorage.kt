package kozyriatskyi.anton.sked.repository

import io.reactivex.Observable
import kozyriatskyi.anton.sked.data.pojo.Day
import kozyriatskyi.anton.sked.data.pojo.LessonDb

interface ScheduleStorage {

    // date in dd.MM.yyyy format
    fun getLessonsByDate(dayNumber: Int, weekNumber: Int, date: String): Observable<Day>

    fun saveLessons(lessons: List<LessonDb>)

    fun getAmountOfLessonsOnDate(date: String): Observable<Int>
}