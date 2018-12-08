package kozyriatskyi.anton.sked.data.repository

import dagger.Lazy
import kozyriatskyi.anton.sked.data.pojo.LessonNetwork
import kozyriatskyi.anton.sked.data.pojo.Student
import kozyriatskyi.anton.sked.data.pojo.Teacher
import kozyriatskyi.anton.sked.data.pojo.User
import kozyriatskyi.anton.sked.repository.ScheduleProvider
import kozyriatskyi.anton.sked.repository.StudentScheduleProvider
import kozyriatskyi.anton.sked.repository.TeacherScheduleProvider
import kozyriatskyi.anton.sked.util.DateUtils

/**
 * Created by Anton on 26.07.2017.
 */
class ParsedScheduleProvider(private val studentScheduleProvider: Lazy<StudentScheduleProvider>,
                             private val teacherScheduleProvider: Lazy<TeacherScheduleProvider>) : ScheduleProvider {

    override fun getSchedule(user: User): List<LessonNetwork> {
        val dateStart = DateUtils.mondayDate()
        val dateEnd = DateUtils.sundayDate(5)

        return when (user) {
            is Student -> {
                val facultyId = user.facultyId
                val courseId = user.courseId
                val groupId = user.groupId
                studentScheduleProvider.get().getSchedule(facultyId, courseId, groupId, dateStart, dateEnd)
            }
            is Teacher -> {
                val departmentId = user.departmentId
                val teacherId = user.teacherId
                teacherScheduleProvider.get().getSchedule(departmentId, teacherId, dateStart, dateEnd)
            }
        }
    }
}