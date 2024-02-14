package com.lyricsnigeria.lyricsnigeria


import android.os.Bundle
import android.view.KeyEvent
//import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty

class AboutFragment : Fragment(), View.OnClickListener {

    private var mExpandBtn: ImageView? = null
    private var mEula: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "About"//make ABOUT title of fragment
        val x = inflater.inflate(R.layout.fragment_about, container, false)

        val toolbar: Toolbar = x.findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "About"

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        mExpandBtn = x.findViewById(R.id.expand_btn)
        mEula = x.findViewById(R.id.eula_text)

        mExpandBtn!!.setOnClickListener(this)

        return x
    }

    override fun onClick(view: View?) {
        when (view!!.id) {

            R.id.expand_btn -> {

                if (mEula!!.visibility == View.GONE) {
                    mEula!!.visibility = View.VISIBLE
                    mExpandBtn!!.setImageResource(R.drawable.ic_expand_less)
                } else {
                    mEula!!.visibility = View.GONE
                    mExpandBtn!!.setImageResource(R.drawable.ic_expand_more)
                }
            }
        }
    }
}
