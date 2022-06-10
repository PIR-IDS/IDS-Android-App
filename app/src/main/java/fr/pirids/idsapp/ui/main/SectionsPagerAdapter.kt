package fr.pirids.idsapp.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.pirids.idsapp.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_services,
    R.string.tab_text_devices,
    R.string.tab_text_network
)

class SectionsPagerAdapter(private val context: Context, fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    /** createFragment is called to instantiate the fragment for the given page.
        Return a PlaceholderFragment (defined as a static inner class below).
    */
    override fun createFragment(position: Int): Fragment = PlaceholderFragment.newInstance(position + 1)

    override fun getItemCount(): Int = TAB_TITLES.size

    fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }
}