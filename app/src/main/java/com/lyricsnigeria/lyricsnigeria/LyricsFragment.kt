package com.lyricsnigeria.lyricsnigeria

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class LyricsFragment : Fragment(){

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    var intItems = 3//***

    //private var imm: InputMethodManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x = inflater.inflate(R.layout.fragment_lyrics, container, false)

        //imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        tabLayout = x.findViewById<View>(R.id.tabs_lyrics) as TabLayout
        viewPager = x.findViewById<View>(R.id.lyrics_viewpager) as ViewPager

        viewPager!!.adapter = MyAdapter(childFragmentManager)
        viewPager!!.offscreenPageLimit = 3

        tabLayout!!.post { tabLayout!!.setupWithViewPager(viewPager) }
        tabLayout!!.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab:TabLayout.Tab) {
                if (viewPager!!.currentItem == 0){
                    tabLayout!!.setTabTextColors(ContextCompat.getColor(context!!, R.color.colorBlack),
                            ContextCompat.getColor(context!!, R.color.color5))
                }else{
                    tabLayout!!.setTabTextColors(ContextCompat.getColor(context!!, R.color.colorBlack),
                            ContextCompat.getColor(context!!, R.color.colorPrimary))
                }
                hideKeyboard(this@LyricsFragment.activity!!)
            }
            override fun onTabUnselected(tab:TabLayout.Tab) {
            }
            override fun onTabReselected(tab:TabLayout.Tab) {
            }
        })

        return x
    }

    internal inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            when (position) {
                0 -> return HotFragment()//***
                1 -> return FeaturedFragment()
                2 -> return TranslatedFragment()
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return intItems
        }

        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return "HOT!"//***
                1 -> return "Featured"
                2 -> return "Translated"
            }
            return null
        }
    }
    fun hideKeyboard(activity: Activity) {//hide keyboard function
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
