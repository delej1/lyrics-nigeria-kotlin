package com.lyricsnigeria.lyricsnigeria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class NewsFragment : Fragment() {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    var intItems = 2//***

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //activity?.title = "Home"//make HOME title of fragment

        val x = inflater.inflate(R.layout.fragment_news, container, false)
        tabLayout = x.findViewById<View>(R.id.tabs_news) as TabLayout
        viewPager = x.findViewById<View>(R.id.news_viewpager) as ViewPager

        viewPager!!.adapter = MyAdapter(childFragmentManager)

        tabLayout!!.post { tabLayout!!.setupWithViewPager(viewPager) }

        return x
    }

    internal inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            when (position) {
                0 -> return EntertainmentFragment()//***
                1 -> return OtherFragment()
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return intItems
        }

        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return "Entertainment"//***
                1 -> return "Other News"
            }
            return null
        }
    }
}
