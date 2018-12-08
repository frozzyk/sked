package kozyriatskyi.anton.sked.repository

import kozyriatskyi.anton.sked.data.pojo.LessonNetwork

/**
 * Created by Anton on 07.07.2017.
 */
interface StudentScheduleProvider {
    fun getSchedule(facultyId: String, courseId: String, groupId: String,
                    dateStart: String, dateEnd: String): List<LessonNetwork>
}

