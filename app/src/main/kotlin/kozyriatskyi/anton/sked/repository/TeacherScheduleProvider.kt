package kozyriatskyi.anton.sked.repository

import kozyriatskyi.anton.sked.data.pojo.LessonNetwork

interface TeacherScheduleProvider {
    fun getSchedule(departmentId: String, teacherId: String,
                    dateStart: String, dateEnd: String): List<LessonNetwork>
}