package kozyriatskyi.anton.sked.di.module

import dagger.Lazy
import dagger.Module
import dagger.Provides
import kozyriatskyi.anton.sked.data.provider.ParsedStudentScheduleProvider
import kozyriatskyi.anton.sked.data.provider.ParsedTeacherScheduleProvider
import kozyriatskyi.anton.sked.data.repository.ParsedScheduleProvider
import kozyriatskyi.anton.sked.di.App
import kozyriatskyi.anton.sked.repository.ScheduleProvider
import kozyriatskyi.anton.sked.repository.StudentScheduleProvider
import kozyriatskyi.anton.sked.repository.TeacherScheduleProvider
import kozyriatskyi.anton.sutparser.StudentScheduleParser
import kozyriatskyi.anton.sutparser.TeacherScheduleParser

/**
 * Created by Anton on 26.07.2017.
 */

@Module
class ScheduleProviderModule {

    @App
    @Provides
    fun provideNetworkStudentScheduleLoader(): StudentScheduleProvider {
        return ParsedStudentScheduleProvider(StudentScheduleParser())
    }

    @App
    @Provides
    fun provideNetworkTeacherScheduleLoader(): TeacherScheduleProvider {
        return ParsedTeacherScheduleProvider(TeacherScheduleParser())
    }

    @App
    @Provides
    fun provideScheduleLoader(studentScheduleLoader: Lazy<StudentScheduleProvider>,
                              teacherScheduleLoader: Lazy<TeacherScheduleProvider>): ScheduleProvider {
        return ParsedScheduleProvider(studentScheduleLoader, teacherScheduleLoader)
    }
}