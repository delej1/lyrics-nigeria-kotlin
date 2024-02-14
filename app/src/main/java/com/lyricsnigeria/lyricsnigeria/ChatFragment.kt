package com.lyricsnigeria.lyricsnigeria


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class ChatFragment : Fragment() {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    var intItems = 1//***

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x = inflater.inflate(R.layout.fragment_chat, container, false)


        tabLayout = x.findViewById<View>(R.id.tabs_chat) as TabLayout
        viewPager = x.findViewById<View>(R.id.chat_viewpager) as ViewPager

        viewPager!!.adapter = MyAdapter(childFragmentManager)

        tabLayout!!.post { tabLayout!!.setupWithViewPager(viewPager) }

        return x
    }

    internal inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            when (position) {
                0 -> return ChatRoomFragment()//***
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return intItems
        }

        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return "Chat Rooms"//***

            }
            return null
        }
    }


}
