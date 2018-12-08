package kozyriatskyi.anton.sked.data.pojo

import kozyriatskyi.anton.sked.data.repository.DateFormatter

/**
 * Created by Anton on 22.08.2017.
 */
class Day(val dayNumber: Int, val weekNumber: Int, val date: String, val lessons: List<LessonDb>)

class DayUi(val dayNum: Int, val weekNumber: Int, val day: String, val shortDate: String,
            val lessons: List<LessonUi>)

class DayMapper(private val lessonMapper: LessonMapper, private val dateFormatter: DateFormatter) {

    fun dbToUi(day: Day): DayUi =
            DayUi(day.dayNumber, day.weekNumber, dateFormatter.dayOfWeek(day.dayNumber),
                    dateFormatter.shortDate(day.date), lessonMapper.dbToView(day.lessons))
}