package kozyriatskyi.anton.sked.byweek

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import kozyriatskyi.anton.sked.week.WeekViewFragment

class WeeksAdapter(childFragmentManager: FragmentManager, private val titles: Array<String>)
    : FragmentStatePagerAdapter(childFragmentManager) {

    companion object {
        private const val DEFAULT_TABS_COUNT = 6
    }

    override fun getItem(i: Int): Fragment = WeekViewFragment.create(i - 1)

    override fun getCount(): Int = DEFAULT_TABS_COUNT

    override fun getPageTitle(position: Int): CharSequence = titles[position]

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
}