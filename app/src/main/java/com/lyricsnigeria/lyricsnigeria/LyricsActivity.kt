package com.lyricsnigeria.lyricsnigeria

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import io.opencensus.internal.StringUtils
import java.io.IOException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*


class LyricsActivity : AppCompatActivity(), View.OnClickListener, Player.EventListener {
    private var mLyricsKey: String? = null
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
    private var revealBtn: ImageView? = null
    private var concealBtn: ImageView? = null
    private var btnCase: LinearLayout? = null

    private var recordBtn: ImageView? = null
    private var stopRecordBtn: ImageView? = null
    private var playRecordBtn: ImageView? = null
    private var stopPlayRecordBtn: ImageView? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
        } else {
            if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME) {
                setTheme(R.style.NightTheme)
            } else {
                setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_lyrics)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Featured")
        mLyricsKey = intent.extras!!.getString("Lyrics_id")

        mCover = findViewById(R.id.cover_lyrics_activity)
        mArtist = findViewById(R.id.artist_lyrics_activity)
        mLyrics = findViewById(R.id.lyrics_body)
        mSong = findViewById(R.id.song_lyrics_activity)
        mUploader = findViewById(R.id.uploader_lyrics_activity)
        mBeat = findViewById(R.id.beat_lyrics_activity)
        mPlayBtn = findViewById(R.id.play_btn)
        mStopBtn = findViewById(R.id.stop_btn)
        mPauseBtn = findViewById(R.id.pause_btn)
        mRewindBtn = findViewById(R.id.rewind_btn)
        mForwardBtn = findViewById(R.id.fast_btn)
        mRepeatBtn = findViewById(R.id.restart_btn)
        simpleSeekBar = findViewById(R.id.lyrics_exo_seekbar)
        mCurrentTime = findViewById(R.id.lyrics_current_time_text)
        mDuration = findViewById(R.id.lyrics_duration_text)

        revealBtn = findViewById(R.id.reveal_btn)
        concealBtn = findViewById(R.id.conceal_btn)
        shareBtn = findViewById(R.id.share_btn)
        btnCase = findViewById(R.id.record_btn_case)

        recordBtn = findViewById(R.id.record_btn)
        stopRecordBtn = findViewById(R.id.stop_record_btn)
        playRecordBtn = findViewById(R.id.play_record_btn)
        stopPlayRecordBtn = findViewById(R.id.stop_play_record_btn)


        val dateTime = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())//create val for date to store audio recorded file with
        audioFilePath = Environment.getExternalStorageDirectory().absolutePath //path to store recorded file
        audioFilePath += "/LyricsNigeria_$dateTime.mp3" //recorded file name


        val toolbar: Toolbar = findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "Lyrics Nigeria"
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }//set back button on toolbar

        mDatabase!!.child(mLyricsKey!!).addValueEventListener(object : ValueEventListener {//set lyrics body to firebase database value

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val lyricsArtist = dataSnapshot.child("artist").value.toString()
                val lyricsSong = dataSnapshot.child("song").value.toString()
                val lyricsCover = dataSnapshot.child("cover").value.toString()
                val lyricsUploader = dataSnapshot.child("uploader").value.toString().replace("\\n", "\n")
                val lyricBody = dataSnapshot.child("lyrics").value.toString().replace("\\n", "\n")
                val lyricsBeat = dataSnapshot.child("beat").value.toString()//get beat from database

                mArtist!!.text = lyricsArtist
                mLyrics!!.text = lyricBody
                mSong!!.text = lyricsSong
                mUploader!!.text = lyricsUploader
                mBeat!!.text = lyricsBeat//create dummy invisible text view and set the beat from database to text in order to feed exo player uri data source

                Picasso.get().load(lyricsCover).into(mCover)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        mPlayBtn!!.setOnClickListener(this)
        mStopBtn!!.setOnClickListener(this)
        mPauseBtn!!.setOnClickListener(this)
        mRewindBtn!!.setOnClickListener(this)
        mForwardBtn!!.setOnClickListener(this)
        mRepeatBtn!!.setOnClickListener(this)

        shareBtn!!.setOnClickListener(this)
        revealBtn!!.setOnClickListener(this)
        concealBtn!!.setOnClickListener(this)

        recordBtn!!.setOnClickListener(this)
        stopRecordBtn!!.setOnClickListener(this)
        playRecordBtn!!.setOnClickListener(this)
        stopPlayRecordBtn!!.setOnClickListener(this)
        playRecordBtn!!.isEnabled = false
        recordBtn!!.isEnabled = true
        stopRecordBtn!!.isEnabled = false

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
        mDuration!!.text = SimpleDateFormat("mm:ss").format(java.sql.Date(duration))
    }

    private fun recordAudio() {//create function to record audio
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder!!.reset()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            mediaRecorder!!.setOutputFile(audioFilePath)
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()

            stopRecordBtn!!.isEnabled = true
            Toasty.normal(this@LyricsActivity, "Recording Audio").show()
            recordBtn!!.isEnabled = false
            isRecording = true

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecord() {//stop recording
        try {
            if (isRecording) {
                mediaRecorder!!.stop()
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
                isRecording = false

                recordBtn!!.isEnabled = true
                stopRecordBtn!!.isEnabled = false
                playRecordBtn!!.isEnabled = true
                Toasty.normal(this@LyricsActivity, "Recording Saved").show()
            } else {
                recordBtn!!.isEnabled = true
                stopRecordBtn!!.isEnabled = false
                playRecordBtn!!.isEnabled = false
                Toasty.normal(this@LyricsActivity, "Not recording anything").show()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun playAudio() {//play recording
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(audioFilePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            mediaPlayer!!.setOnCompletionListener { mp ->
                stopAudio()
            }
            if (exoPlayer != null && exoPlayer!!.playWhenReady) {//if exo player is not null and is playing
                releaseExoplayer()
            }
            playRecordBtn!!.isEnabled = false
            playRecordBtn!!.isVisible = false
            recordBtn!!.isEnabled = false
            stopRecordBtn!!.isEnabled = false
            stopPlayRecordBtn!!.isVisible = true
            stopPlayRecordBtn!!.isEnabled = true
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopAudio() {//stop playing recording
        try {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null

            playRecordBtn!!.isEnabled = false
            playRecordBtn!!.isVisible = true
            recordBtn!!.isEnabled = true
            stopRecordBtn!!.isEnabled = false
            stopPlayRecordBtn!!.isVisible = false
            stopPlayRecordBtn!!.isEnabled = false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.reveal_btn -> {
                revealBtn!!.isVisible = false
                concealBtn!!.isVisible = true
                btnCase!!.isVisible = true
            }
            R.id.conceal_btn -> {
                concealBtn!!.isVisible = false
                btnCase!!.isVisible = false
                revealBtn!!.isVisible = true
            }
            R.id.play_btn -> {
                if (isNetworkAvailable() && !(exoPlayer != null && exoPlayer!!.playWhenReady) && mBeat!!.text.toString().isNotEmpty() && !isPaused) {
                    initializeExoplayer()

                    mPlayBtn!!.isVisible = false
                    mPauseBtn!!.isVisible = true
                    isPaused = false
                    Toasty.normal(this@LyricsActivity, "Streaming Instrumental").show()
                } else {
                    if (!isNetworkAvailable()) {
                        Toasty.normal(this@LyricsActivity, "No Internet Connection").show()
                    } else {
                        if (isNetworkAvailable() && mBeat!!.text.toString().isEmpty()) {
                            Toasty.normal(this@LyricsActivity, "No Media to Stream").show()
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
            R.id.pause_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady) {//if exo player is not null and is playing
                    exoPlayer!!.playWhenReady = false
                    mPlayBtn!!.isVisible = true
                    mPauseBtn!!.isVisible = false
                    isPaused = true

                    val duration = (if (exoPlayer == null) 0 else exoPlayer!!.duration).toLong()
                    val position = (if (exoPlayer == null) 0 else exoPlayer!!.currentPosition).toLong()
                    val elapsed = (duration - position)
                    val elapsedTxt = SimpleDateFormat("mm:ss").format(java.sql.Date(elapsed))
                    mDuration!!.text = elapsedTxt
                } else {
                }
            }
            R.id.rewind_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null or is playing
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition - 5000)
                } else {
                }

            }
            R.id.fast_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition + 5000)
                } else {
                }

            }
            R.id.restart_btn -> {
                if (exoPlayer != null && exoPlayer!!.currentPosition > 0 && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    exoPlayer!!.seekTo(0)
                } else {
                }

            }
            R.id.stop_btn -> {
                if (exoPlayer != null && exoPlayer!!.playWhenReady || isPaused) {//if exo player is not null and is playing
                    releaseExoplayer()
                    mPlayBtn!!.isVisible = true
                    mPauseBtn!!.isVisible = false
                    isPaused = false
                } else {
                }
            }

            R.id.record_btn -> {
                if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permissions, 0)
                } else {
                    recordAudio()
                }
            }
            R.id.stop_record_btn -> {
                stopRecord()
            }
            R.id.play_record_btn -> {
                playAudio()
            }
            R.id.stop_play_record_btn -> {
                stopAudio()
            }
            R.id.share_btn -> {
                setShareIntent()//call share Intent
            }
        }
    }

    private fun setShareIntent() {//create function for Share Intent

        mDatabase!!.child(mLyricsKey!!).addValueEventListener(object : ValueEventListener {
            //set share subject/body to firebase database value
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val shareSub = "LyricsNigeria: " + dataSnapshot.child("song").value.toString()
                val shareBody = dataSnapshot.child("lyrics").value.toString().replace("\\n", "\n")
                val myIntent = Intent(Intent.ACTION_SEND)
                myIntent.setType("text/plain")

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
        } else {
            if (mediaRecorder != null && isRecording) {
                stopRecord()
            } else {
                if (mediaPlayer != null) {
                    stopAudio()
                }
            }
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
            if (mediaRecorder != null && isRecording) {
                stopRecord()
                finish()
            } else {
                if (mediaPlayer != null) {
                    stopAudio()
                    finish()
                } else {
                    finish()
                }
            }
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
            val elapsedTxt = SimpleDateFormat("mm:ss").format(java.sql.Date(elapsed))
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
        val elapsedTxt = SimpleDateFormat("mm:ss").format(java.sql.Date(elapsed))
        mDuration!!.text = elapsedTxt
        simpleSeekBar!!.progress = position.toInt()
        val currentTimeTxt = SimpleDateFormat("mm:ss").format(java.sql.Date(position))
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

