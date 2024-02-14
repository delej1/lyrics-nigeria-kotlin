package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import java.sql.Date
import java.text.SimpleDateFormat


class HotActivity : AppCompatActivity(), View.OnClickListener, Player.EventListener {
    private var mHotKey: String? = null
    private var mDatabase: DatabaseReference? = null
    private var mCover: ImageView? = null
    private var mArtist: TextView? = null
    private var mLyrics: TextView? = null
    private var mSong: TextView? = null
    private var mUploader: TextView? = null
    private var mBeat: TextView? = null
    private var mPlayBtn: ImageView? = null
    private var mPauseBtn: ImageView? = null
    private var mRewindBtn: ImageView? = null
    private var mForwardBtn: ImageView? = null
    private var mRepeatBtn: ImageView? = null
    private var mStopBtn: ImageView? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var mCurrentTime: TextView? = null
    private var mDuration: TextView? = null

    private var shareBtn: ImageView? = null
    private var isPaused: Boolean = false

    private var simpleSeekBar: SeekBar? = null

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
        setContentView(R.layout.activity_hot)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Hot")
        mHotKey = intent.extras!!.getString("Hot_id")

        mCover = findViewById(R.id.cover_hot_activity)
        mArtist = findViewById(R.id.artist_hot_activity)
        mLyrics = findViewById(R.id.hot_body)
        mSong = findViewById(R.id.song_hot_activity)
        mUploader = findViewById(R.id.uploader_hot_activity)
        mBeat = findViewById(R.id.beat_hot_activity)
        mPlayBtn = findViewById(R.id.hot_play_btn)
        mPauseBtn = findViewById(R.id.hot_pause_btn)
        mRewindBtn = findViewById(R.id.hot_rewind_btn)
        mForwardBtn = findViewById(R.id.hot_fast_btn)
        mRepeatBtn = findViewById(R.id.hot_restart_btn)
        mStopBtn = findViewById(R.id.hot_stop_btn)
        shareBtn = findViewById(R.id.hot_share_btn)
        simpleSeekBar = findViewById(R.id.exo_seekbar)
        mCurrentTime = findViewById(R.id.current_time_text)
        mDuration = findViewById(R.id.duration_text)

        val toolbar: Toolbar = findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "Lyrics Nigeria"
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }//set back button on toolbar

        mDatabase!!.child(mHotKey!!).addValueEventListener(object : ValueEventListener {//set lyrics body to firebase database value

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val hotArtist = dataSnapshot.child("artist").value.toString()
                val hotSong = dataSnapshot.child("song").value.toString()
                val hotCover = dataSnapshot.child("cover").value.toString()
                val hotUploader = dataSnapshot.child("uploader").value.toString().replace("\\n", "\n")
                val hotBody = dataSnapshot.child("lyrics").value.toString().replace("\\n", "\n")
                val hotBeat = dataSnapshot.child("beat").value.toString()//get beat from database

                mArtist!!.text = hotArtist
                mLyrics!!.text = hotBody
                mSong!!.text = hotSong
                mUploader!!.text = hotUploader
                mBeat!!.text = hotBeat//create dummy invisible text view and set the beat from database to text in order to feed exo player uri data source

                Picasso.get().load(hotCover).into(mCover)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        mPlayBtn!!.setOnClickListener(this)
        mPauseBtn!!.setOnClickListener(this)
        mRewindBtn!!.setOnClickListener(this)
        mForwardBtn!!.setOnClickListener(this)
        mRepeatBtn!!.setOnClickListener(this)
        mStopBtn!!.setOnClickListener(this)

        shareBtn!!.setOnClickListener(this)
    }

    private fun isNetworkAvailable(): Boolean {//check if internet connection exists
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }//if exists == true else == false

    private fun initializeExoplayer() {//implement exoplayer

        val renderersFactory = DefaultRenderersFactory(this, null, //drmSessionManager: DrmSessionManager
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        val trackSelector = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector)

        val userAgent = Util.getUserAgent(this, "ExoPlayerIntro")
        val mediaSource = ExtractorMediaSource(
                Uri.parse(mBeat!!.text.toString()),//set uri string to instrumental gotten from each respective key value
                DefaultDataSourceFactory(this, userAgent),
                DefaultExtractorsFactory(),
                null, // eventHandler: Handler
                null) // eventListener: ExtractorMediaSource.EventListener


        exoPlayer!!.addListener(this)
        exoPlayer!!.prepare(mediaSource)
        exoPlayer!!.playWhenReady = true
        isPaused = false
    }

    private fun releaseExoplayer() {
        exoPlayer!!.release()
        exoPlayer!!.playWhenReady = false

        exoPlayer!!.seekTo(0)
        val duration = (if (exoPlayer == null) 0 else exoPlayer!!.duration).toLong()
        mDuration!!.text = SimpleDateFormat("mm:ss").format(Date(duration))
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.hot_play_btn -> {
                if (isNetworkAvailable() && !(exoPlayer != null && exoPlayer!!.playWhenReady) && !(mBeat!!.text.toString().isEmpty()) && !isPaused) {
                    initializeExoplayer()

                    mPlayBtn!!.isVisible = false
                    mPauseBtn!!.isVisible = true
                    isPaused = false
                    Toasty.normal(this@HotActivity, "Streaming Song").show()
                } else {
                    if (!isNetworkAvailable()) {
                        Toasty.normal(this@HotActivity, "No Internet Connection").show()
                    } else {
                        if (isNetworkAvailable() && mBeat!!.text.toString().isEmpty()) {
                            Toasty.normal(this@HotActivity, "No Media to Stream").show()
                        } else {
                            if (isPaused) {
                                exoPlayer!!.playWhenReady = true
                                mPlayBtn!!.isVisible = false
                                mPauseBtn!!.isVisible = true
                                isPaused = false
                            }
                        }
                    }
                }
            }
            R.id.hot_pause_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady) {//if exo player is not null and is playing
                    exoPlayer!!.playWhenReady = false
                    mPlayBtn!!.isVisible = true
                    mPauseBtn!!.isVisible = false
                    isPaused = true

                    val duration = (if (exoPlayer == null) 0 else exoPlayer!!.duration).toLong()
                    val position = (if (exoPlayer == null) 0 else exoPlayer!!.currentPosition).toLong()
                    val elapsed = (duration - position)
                    val elapsedTxt = SimpleDateFormat("mm:ss").format(Date(elapsed))
                    mDuration!!.text = elapsedTxt
                } else {
                }
            }
            R.id.hot_rewind_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition - 5000)
                } else {
                }

            }
            R.id.hot_fast_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition + 5000)
                } else {
                }

            }
            R.id.hot_restart_btn -> {
                if (exoPlayer != null && exoPlayer!!.currentPosition > 0 && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    exoPlayer!!.seekTo(0)
                } else {
                }

            }
            R.id.hot_stop_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    releaseExoplayer()
                    mPlayBtn!!.isVisible = true
                    mPauseBtn!!.isVisible = false
                    isPaused = false
                } else {
                }
            }
            R.id.hot_share_btn -> {
                setShareIntent()//call share Intent
            }
        }
    }


    private fun setShareIntent() {//create function for Share Intent

        mDatabase!!.child(mHotKey!!).addValueEventListener(object : ValueEventListener {
            //set share subject/body to firebase database value
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val shareSub = "LyricsNigeria: " + dataSnapshot.child("song").value.toString()
                val shareBody = dataSnapshot.child("lyrics").value.toString().replace("\\n", "\n")
                val myIntent = Intent(Intent.ACTION_SEND)
                myIntent.type = "text/plain"

                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(myIntent, "Share Lyrics"))
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    override fun onStop() {
        super.onStop()
        if (exoPlayer != null && exoPlayer!!.playWhenReady) {//if exo player is not null and is playing
            releaseExoplayer()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }//implement back button on toolbar

    override fun onBackPressed() {

        if (exoPlayer != null && exoPlayer!!.playWhenReady) {
            releaseExoplayer()
            finish()
        } else {
            finish()
        }
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity() {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        updateProgressBar()
        if (playbackState == Player.STATE_ENDED) {
            releaseExoplayer()
            mPlayBtn!!.isVisible = true
            mPauseBtn!!.isVisible = false
            isPaused = false

            val duration = (if (exoPlayer == null) 0 else exoPlayer!!.duration).toLong()
            val position = (if (exoPlayer == null) 0 else exoPlayer!!.currentPosition).toLong()
            val elapsed = (duration - position)
            val elapsedTxt = SimpleDateFormat("mm:ss").format(Date(elapsed))
            mDuration!!.text = elapsedTxt
        }
    }

    private fun updateProgressBar() {
        val handler = Handler()
        val duration = (if (exoPlayer == null) 0 else exoPlayer!!.duration).toLong()
        val position = (if (exoPlayer == null) 0 else exoPlayer!!.currentPosition).toLong()
        val bufferedPosition = (if (exoPlayer == null) 0 else exoPlayer!!.bufferedPosition).toLong()
        val playbackState = if (exoPlayer == null) Player.STATE_IDLE else exoPlayer!!.playbackState
        val elapsed = (duration - position)


        simpleSeekBar!!.max = duration.toInt()
        val elapsedTxt = SimpleDateFormat("mm:ss").format(Date(elapsed))
        mDuration!!.text = elapsedTxt
        simpleSeekBar!!.progress = position.toInt()
        val currentTimeTxt = SimpleDateFormat("mm:ss").format(Date(position))
        mCurrentTime!!.text = currentTimeTxt
        simpleSeekBar!!.secondaryProgress = bufferedPosition.toInt()

        // Remove scheduled updates.
        handler.removeCallbacks(updateProgressAction)

        // Schedule an update if necessary.
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs: Long
            if (exoPlayer!!.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs = 1000 - (position % 1000)
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            handler.postDelayed(updateProgressAction, delayMs)
        }
    }

    private val updateProgressAction = Runnable { updateProgressBar() }
}
