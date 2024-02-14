package com.lyricsnigeria.lyricsnigeria

import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class TranslatedLyrics : AppCompatActivity() {

    private var mLyricsKey: String? = null
    private var mDatabase: DatabaseReference? = null
    private var mCover: ImageView? = null
    private var mArtist: TextView? = null
    private var mLyrics: TextView? = null
    private var mSong: TextView? = null
    private var mUploader: TextView? = null

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
        setContentView(R.layout.activity_translated_lyrics)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Translated")

        mLyricsKey = intent.extras!!.getString("Translated_id")

        mCover = findViewById(R.id.cover_translated_lyrics)
        mArtist = findViewById(R.id.artist_translated_lyrics)
        mLyrics = findViewById(R.id.translated_lyrics_body)
        mSong = findViewById(R.id.song_translated_lyrics)
        mUploader = findViewById(R.id.uploader_translated_lyrics)


        val toolbar: Toolbar = findViewById(R.id.toolbar) //make toolbar value and get xml toolbar id
        toolbar.title = "Lyrics Nigeria"
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }//set back button on toolbar

        mDatabase!!.child(mLyricsKey!!).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val lyricsArtsit = dataSnapshot.child("artist").value.toString()
                val lyricsSong = dataSnapshot.child("song").value.toString()
                val lyricsCover = dataSnapshot.child("cover").value.toString()
                val lyricsUploader = dataSnapshot.child("uploader").value.toString().replace("\\n", "\n")
                val lyricBody = dataSnapshot.child("lyrics").value.toString().replace("\\n", "\n")

                mArtist!!.text = lyricsArtsit
                mLyrics!!.text = lyricBody
                mSong!!.text = lyricsSong
                mUploader!!.text = lyricsUploader

                Picasso.get().load(lyricsCover).into(mCover)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }//implement back button on toolbar

}

