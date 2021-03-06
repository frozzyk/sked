package kozyriatskyi.anton.sked.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kozyriatskyi.anton.sked.R
import kozyriatskyi.anton.sked.customview.LessonDetailsSheet
import kozyriatskyi.anton.sked.data.pojo.DayUi
import kozyriatskyi.anton.sked.data.pojo.LessonUi
import kozyriatskyi.anton.sked.di.Injector
import kozyriatskyi.anton.sked.util.find
import kozyriatskyi.anton.sked.util.inflate
import kozyriatskyi.anton.sked.util.toast
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import moxy.presenter.ProvidePresenterTag
import javax.inject.Inject


/**
 * Created by Anton on 01.08.2017.
 */
class DayViewFragment : MvpAppCompatFragment(), DayView, DayLessonsAdapter.OnLessonClickListener {

    companion object {
        private const val EXTRA_DAY_NUM = "day_num"
        private const val EXTRA_NEXT_WEEK = "next_week"

        fun create(dayNumber: Int, isNextWeek: Boolean): DayViewFragment {
            val fragment = DayViewFragment()
            val arguments = Bundle()

            arguments.putInt(EXTRA_DAY_NUM, dayNumber)
            arguments.putBoolean(EXTRA_NEXT_WEEK, isNextWeek)

            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var adapter: DayLessonsAdapter

    @Inject
    @InjectPresenter
    lateinit var presenter: DayViewPresenter

    @ProvidePresenterTag(presenterClass = DayViewPresenter::class)
    fun provideTag(): String = arguments!!.getInt(EXTRA_DAY_NUM).toString()

    @ProvidePresenter
    fun providePresenter(): DayViewPresenter {
        val arguments = arguments
                ?: throw  IllegalArgumentException("Arguments must not be null and must contain " +
                        "EXTRA_DAY_NUM and EXTRA_NEXT_WEEK")

        val dayNumber = arguments.getInt(EXTRA_DAY_NUM)
        val isNextWeek = arguments.getBoolean(EXTRA_NEXT_WEEK)
        Injector.inject(this, dayNumber, isNextWeek)
        return presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = container!!.inflate(R.layout.fragment_lessons)

        val recycler = rootView.find<RecyclerView>(R.id.lessons_recycler)
        recycler.layoutManager = LinearLayoutManager(context)
        adapter = DayLessonsAdapter(this)
        recycler.adapter = adapter

        return rootView
    }

    override fun onLessonClick(lesson: LessonUi) {
        presenter.onLessonClick(lesson)
    }

    override fun showDay(day: DayUi) {
        adapter.updateData(day)
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun showStudentLessonDetails(lesson: LessonUi) {
        val sheet = LessonDetailsSheet.create(lesson, LessonDetailsSheet.USER_TYPE_STUDENT)
        sheet.show(activity!!.supportFragmentManager, LessonDetailsSheet.TAG)
    }

    override fun showTeacherLessonDetails(lesson: LessonUi) {
        val sheet = LessonDetailsSheet.create(lesson, LessonDetailsSheet.USER_TYPE_TEACHER)
        sheet.show(activity!!.supportFragmentManager, LessonDetailsSheet.TAG)
    }
}