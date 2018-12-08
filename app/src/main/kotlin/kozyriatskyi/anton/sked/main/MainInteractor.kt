package kozyriatskyi.anton.sked.main

import io.reactivex.Completable
import io.reactivex.Single
import kozyriatskyi.anton.sked.data.pojo.LessonMapper
import kozyriatskyi.anton.sked.data.pojo.LessonNetwork
import kozyriatskyi.anton.sked.data.repository.UserInfoStorage
import kozyriatskyi.anton.sked.repository.ScheduleProvider
import kozyriatskyi.anton.sked.repository.ScheduleStorage

class MainInteractor(private val scheduleStorage: ScheduleStorage,
                     private val lessonMapper: LessonMapper,
                     private val scheduleLoader: ScheduleProvider,
                     private val userInfoStorage: UserInfoStorage) {

    fun updateSchedule(): Completable = Single.create<List<LessonNetwork>> {
        try {
            val schedule = scheduleLoader.getSchedule(userInfoStorage.getUser())
            if (it.isDisposed.not()) {
                it.onSuccess(schedule)
            }
        } catch (t: Throwable) {
            if (it.isDisposed.not()) {
                it.onError(t)
            }
        }
    }
            .map(lessonMapper::networkToDb)
            .flatMapCompletable { lessons ->
                Completable.fromAction { scheduleStorage.saveLessons(lessons) }
            }
}