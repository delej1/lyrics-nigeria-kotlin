package com.lyricsnigeria.lyricsnigeria


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import java.lang.Exception

class FavouritesFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mProcessFav: Boolean = false
    private var mDatabaseFav: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var text: TextView? = null
    private var lastFirstVisiblePosition: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x: View = inflater.inflate(R.layout.fragment_favourites, container, false)

        text = x.findViewById(R.id.favourites_no_added)

        mAuth = FirebaseAuth.getInstance()


        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            mDatabaseFav = FirebaseDatabase.getInstance().reference.child("Favourites").child(mAuth!!.currentUser!!.uid)

            mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

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

            mDatabaseFav!!.keepSynced(true)

            mRecyclerView = x.findViewById(R.id.favourites_recycler_view) as RecyclerView
            mRecyclerView!!.setHasFixedSize(true)
            mRecyclerView!!.layoutManager = LinearLayoutManager(this@FavouritesFragment.context)
        }

        loadFavourites()
        return x
    }

    private fun loadFavourites(){
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FavouritesAdapter, LyricsViewHolder>(FavouritesAdapter::class.java, R.layout.layout_listitem, LyricsViewHolder::class.java, mDatabaseFav) {
                override fun populateViewHolder(viewHolder: LyricsViewHolder, model: FavouritesAdapter, position: Int) {

                    val lyricsKey = getRef(position).key.toString()
                    viewHolder.setArtist(model.artist!!)
                    viewHolder.setGenre(model.genre!!)
                    viewHolder.setSong(model.song!!)
                    viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                    viewHolder.favourties()
                    viewHolder.setFavBtn(lyricsKey)
                    //processing event for click on fav button/image
                    viewHolder.mFavBtn!!.setOnClickListener {
                        mProcessFav = true

                        mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (mProcessFav) {

                                    if (dataSnapshot.hasChild(lyricsKey)) {

                                        mDatabaseFav!!.child(lyricsKey).removeValue()
                                        mProcessFav = false
                                    } else {
                                        mProcessFav = false
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })

                    }
                    //processing event for click on cardview
                    viewHolder.mView.setOnClickListener {
                        val lyricsIntent = Intent(this@FavouritesFragment.context, FavouriteLyrics::class.java)
                        lyricsIntent.putExtra("Lyrics_id", lyricsKey)
                        startActivity(lyricsIntent)

                    }
                }
            }
            mRecyclerView!!.adapter = firebaseRecyclerAdapter
        }else{
            text!!.visibility = View.VISIBLE
        }
    }

     class LyricsViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {

        var mFavBtn: ImageView? = null
        private var mDatabaseFav: DatabaseReference? = null
        private var mAuth: FirebaseAuth? = null


        fun favourties() {
            mFavBtn = mView.findViewById<View>(R.id.favourite_btn) as ImageView
        }

        fun setFavBtn(lyricsKey: String) {

            mAuth = FirebaseAuth.getInstance()
            mDatabaseFav = FirebaseDatabase.getInstance().reference.child("Favourites").child(mAuth!!.currentUser!!.uid)
            mDatabaseFav!!.keepSynced(true)

            mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.hasChild(lyricsKey)) {

                        mFavBtn!!.setImageResource(R.mipmap.favorite_red)
                    } else {
                        mFavBtn!!.setImageResource(R.mipmap.favorite_grey)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

        fun setArtist(artist: String) {
            val songArtist = mView.findViewById<View>(R.id.artist) as TextView
            songArtist.text = artist
        }

        fun setGenre(genre: String) {
            val songGenre = mView.findViewById<View>(R.id.genre) as TextView
            songGenre.text = genre
        }

        fun setSong(song: String) {
            val songSong = mView.findViewById<View>(R.id.song_title) as TextView
            songSong.text = song
        }

        fun setCover(ctx: Context, cover: String) {
            val songCover = mView.findViewById<View>(R.id.music_cover) as CircleImageView
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

    override fun onResume() {
        super.onResume()

        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val vto = mRecyclerView!!.viewTreeObserver
            vto.addOnGlobalLayoutListener { (mRecyclerView!!.layoutManager as LinearLayoutManager).scrollToPosition(lastFirstVisiblePosition) }//scrolls to last position on resume

            Handler().postDelayed(Runnable { lastFirstVisiblePosition = -1 }, 200)//delays reset of lastFirstVisiblePosition by 200 milli seconds
        }

    }

    override fun onPause() {
        super.onPause()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            lastFirstVisiblePosition = (mRecyclerView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()//stores last position on pause
        }
    }
}