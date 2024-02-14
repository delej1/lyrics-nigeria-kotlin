package com.lyricsnigeria.lyricsnigeria


import android.content.Intent
import android.net.Uri
import android.os.Bundle
//import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty

class FeedbackFragment : Fragment(), View.OnClickListener {

    private var mFdbSub: EditText? = null
    private var mFdbMsg: EditText? = null
    private var mSendBtn: Button? = null
    private var mEmail: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        activity?.title = "Feedback"//make FEEDBACK title of fragment

        // Inflate the layout for this fragment
        val x = inflater.inflate(R.layout.fragment_feedback, container, false)

        val toolbar: Toolbar = x.findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "Feedback & Requests"

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        mFdbSub = x.findViewById(R.id.feedback_subject)
        mFdbMsg = x.findViewById(R.id.feedback_msg)
        mSendBtn = x.findViewById(R.id.feedback_send)
        mEmail = "djhsstudio@gmail.com"

        mSendBtn!!.setOnClickListener(this@FeedbackFragment)

        return x
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.feedback_send -> if (mFdbMsg!!.text.toString().trim().isNotEmpty() && mFdbSub!!.text.toString().trim().isNotEmpty()) {//line checks if editText is empty or not
                val feedbackEmail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mEmail"))
                feedbackEmail.putExtra(Intent.EXTRA_SUBJECT, mFdbSub!!.text.toString())
                feedbackEmail.putExtra(Intent.EXTRA_TEXT, mFdbMsg!!.text.toString())
                startActivity(Intent.createChooser(feedbackEmail, "Send Feedback"))
            } else {
                Toasty.normal(this@FeedbackFragment.context!!, "Fill all Fields").show()
            }
        }
    }
}
