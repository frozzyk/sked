package kozyriatskyi.anton.sked.data.provider

import kozyriatskyi.anton.sked.data.pojo.LessonNetwork
import kozyriatskyi.anton.sked.repository.TeacherScheduleProvider
import kozyriatskyi.anton.sutparser.TeacherScheduleParser

/**
 * Created by Anton on 01.08.2017.
 */

class ParsedTeacherScheduleProvider(private val parser: TeacherScheduleParser) : TeacherScheduleProvider {

    override fun getSchedule(departmentId: String, teacherId: String,
                             dateStart: String, dateEnd: String): List<LessonNetwork> {
        return parser.getSchedule(departmentId, teacherId, dateStart, dateEnd)
                .map {
                    LessonNetwork(
                            it.date,
                            it.number,
                            it.type,
                            it.cabinet,
                            it.shortName,
                            it.name,
                            it.addedOnDate,
                            it.addedOnTime,
                            it.who,
                            it.whoShort)
                }
    }
}