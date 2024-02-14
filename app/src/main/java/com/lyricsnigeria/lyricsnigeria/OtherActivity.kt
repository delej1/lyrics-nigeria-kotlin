package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty

class OtherActivity : AppCompatActivity() {

    private var mGistKey: String? = null
    private var mDatabase: DatabaseReference? = null
    private var mCover: ImageView? = null
    private var mImg: PhotoView? = null
    private var mHeadline: TextView? = null
    private var mContent: TextView? = null
    private var mSource: TextView? = null
    private var mVideo: VideoView? = null
    private var vidLayout: RelativeLayout? = null
    private var isPlaying = false

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
        } else {
            if(BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME){
                setTheme(R.style.NightTheme)
            }else{
                setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_gist)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Other News")
        mGistKey = intent.extras!!.getString("Other_id")

        mCover = findViewById(R.id.cover_gist_activity)
        mImg = findViewById(R.id.img_gist_activity)
        mHeadline = findViewById(R.id.headline_gist_activity)
        mContent = findViewById(R.id.gist_body)
        mSource = findViewById(R.id.source_gist_activity)
        mVideo = findViewById(R.id.gist_video_view)
        vidLayout = findViewById(R.id.gist_vid_view_layout)


        val toolbar: Toolbar = findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "Lyrics Nigeria"
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }//set back button on toolbar

        mDatabase!!.child(mGistKey!!).addValueEventListener(object : ValueEventListener {//set lyrics body to firebase database value

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val gistHeadline = dataSnapshot.child("headline").value.toString()
                val gistBody = dataSnapshot.child("body").value.toString().replace("\\n", "\n")
                val gistCover = dataSnapshot.child("cover").value.toString()
                val gistImg = dataSnapshot.child("image").value.toString()
                val gistSource = dataSnapshot.child("source").value.toString().replace("\\n", "\n")
                val videoUri = Uri.parse(dataSnapshot.child("vid").value.toString())

                mHeadline!!.text = gistHeadline
                mContent!!.text = gistBody
                mSource!!.text = gistSource

                Picasso.get().load(gistCover).into(mCover)
                Picasso.get().load(gistImg).into(mImg)

                if ((dataSnapshot.child("vid").value.toString().isEmpty())) {//check if vid path is empty
                    vidLayout!!.isVisible = false
                    mVideo!!.isVisible = false
                } else {
                    vidLayout!!.isVisible = true
                    mVideo!!.isVisible = true
                }

                val mc = MediaController(this@OtherActivity)//calling media controller for pause and rewind function
                mVideo!!.setMediaController(mc)
                vidLayout!!.setOnClickListener {
                    if (!isPlaying && isNetworkAvailable()) {
                        mVideo!!.setVideoURI(videoUri)
                        mVideo!!.requestFocus()
                        mVideo!!.start()
                        isPlaying = true
                        Toasty.normal(this@OtherActivity, "Streaming Video").show()
                    } else {
                        if (isPlaying) {
                            mVideo!!.stopPlayback()
                            mVideo!!.clearFocus()
                            isPlaying = false
                            Toasty.normal(this@OtherActivity, "Video Stopped").show()
                        } else {
                            if (!isNetworkAvailable()) {
                                Toasty.normal(this@OtherActivity, "No Internet Connection").show()
                            }
                        }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {//check if internet connection exists
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }//if exists == true else == false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }//implement back button on toolbar

    override fun onBackPressed() {
        finish()
    }
}
