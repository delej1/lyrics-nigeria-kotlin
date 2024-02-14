package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import java.lang.Exception

class TranslatedFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mSearchField: EditText? = null
    private var lastFirstVisiblePosition: Int = -1
    private var text: TextView? = null

    private var mSearchField2: EditText? = null
    private var searchBack: ImageView? = null
    private var searchCancel: ImageView? = null
    private var imm: InputMethodManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val x: View = inflater.inflate(R.layout.fragment_translated, container, false)

        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        text = x.findViewById(R.id.translation_no_added)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("Translated")

        mDatabase!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    text!!.visibility = View.GONE
                } else {
                    text!!.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        mDatabase!!.keepSynced(true)

        mRecyclerView = x.findViewById(R.id.translated_recycler_view) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this@TranslatedFragment.context)

        mSearchField = x.findViewById(R.id.edit_text_search_translated) as EditText
        mSearchField2 = x.findViewById(R.id.trans_edit_text_search2) as EditText
        searchBack = x.findViewById(R.id.trans_search_back)
        searchCancel = x.findViewById(R.id.trans_search_cancel)

        mSearchField!!.setOnClickListener {
            mSearchField!!.isVisible = false
            mSearchField2!!.isVisible = true
            mSearchField2!!.requestFocus()
            searchBack!!.isVisible = true
            searchCancel!!.isVisible = true
            if (mSearchField2!!.requestFocus()) {
                imm!!.showSoftInput(mSearchField2, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        mSearchField2!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText = mSearchField2!!.text.toString().trim()
                firebaseUserSearch(searchText)
            }
        })

        searchCancel!!.setOnClickListener {
            mSearchField2!!.text.clear()
        }
        searchBack!!.setOnClickListener {
            mSearchField2!!.text.clear()
            mSearchField2!!.clearFocus()
            mSearchField2!!.isVisible = false
            searchBack!!.isVisible = false
            searchCancel!!.isVisible = false
            mSearchField!!.isVisible = true
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }
        loadTranslated()
        return x
    }

    private fun loadTranslated(){
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<TranslatedAdapter, LyricsViewHolder>(TranslatedAdapter::class.java, R.layout.layout_listitem_translated, LyricsViewHolder::class.java, mDatabase) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: TranslatedAdapter, position: Int) {//populating a viewHolder (RecyclerView)

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                viewHolder.setUploader(model.uploader!!)
                //processing event for click on cardview

                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@TranslatedFragment.context, TranslatedLyrics::class.java)
                    lyricsIntent.putExtra("Translated_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter
    }

    class LyricsViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {


        fun setArtist(artist: String) {
            val songArtist = mView.findViewById<View>(R.id.trans_artist) as TextView
            songArtist.text = artist
        }

        fun setGenre(genre: String) {
            val songGenre = mView.findViewById<View>(R.id.trans_genre) as TextView
            songGenre.text = genre
        }

        fun setSong(song: String) {
            val songSong = mView.findViewById<View>(R.id.trans_song_title) as TextView
            songSong.text = song
        }

        fun setUploader(uploader: String) {
            val songUploader = mView.findViewById<View>(R.id.trans_uploader) as TextView
            songUploader.text = uploader
        }

        fun setCover(ctx: Context, cover: String) {
            val songCover = mView.findViewById<View>(R.id.trans_music_cover) as CircleImageView
            Picasso.get()
                    .load(cover)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(songCover, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()
                                    .load(cover)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.ic_error_outline_white_48dp)
                                    .into(songCover, object : Callback {
                                        override fun onSuccess() {
                                        }

                                        override fun onError(e: Exception?) {
                                        }
                                    })
                        }
                    })

        }
    }

    private fun firebaseUserSearch(searchText: String) {

        val query = mDatabase!!.orderByChild("song").startAt(searchText).endAt(searchText + "\uf8ff")

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<TranslatedAdapter, LyricsViewHolder>(TranslatedAdapter::class.java, R.layout.layout_listitem_translated, LyricsViewHolder::class.java, query) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: TranslatedAdapter, position: Int) {

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)

                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@TranslatedFragment.context!!, TranslatedLyrics::class.java)
                    lyricsIntent.putExtra("Translated_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter

    }

    override fun onResume() {
        super.onResume()

        val vto = mRecyclerView!!.viewTreeObserver
        vto.addOnGlobalLayoutListener { (mRecyclerView!!.layoutManager as LinearLayoutManager).scrollToPosition(lastFirstVisiblePosition) }//scrolls to last position on resume

        Handler().postDelayed(Runnable { lastFirstVisiblePosition = -1 }, 200)//delays reset of lastFirstVisiblePosition by 200 milli seconds

        if (mSearchField2!!.requestFocus())//reset search field to normal
        {
            mSearchField2!!.text.clear()
            mSearchField2!!.clearFocus()
            mSearchField2!!.isVisible = false
            searchBack!!.isVisible = false
            searchCancel!!.isVisible = false
            mSearchField!!.isVisible = true
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        lastFirstVisiblePosition = (mRecyclerView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()//stores last position on pause
    }
}

