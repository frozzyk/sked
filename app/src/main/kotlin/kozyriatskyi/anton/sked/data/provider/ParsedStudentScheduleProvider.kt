package kozyriatskyi.anton.sked.data.provider

import kozyriatskyi.anton.sked.data.pojo.LessonNetwork
import kozyriatskyi.anton.sked.repository.StudentScheduleProvider
import kozyriatskyi.anton.sutparser.StudentScheduleParser

/**
 * Created by Anton on 01.08.2017.
 */
class ParsedStudentScheduleProvider(private val parser: StudentScheduleParser) : StudentScheduleProvider {

    override fun getSchedule(facultyId: String, courseId: String, groupId: String,
                             dateStart: String, dateEnd: String): List<LessonNetwork> {
        return parser.getSchedule(facultyId, courseId, groupId, dateStart, dateEnd)
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