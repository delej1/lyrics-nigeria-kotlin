package com.lyricsnigeria.lyricsnigeria


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import java.lang.Exception


class FeaturedFragment : Fragment(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private var mRecyclerView: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    private var mProcessFav: Boolean = false
    private var mDatabaseFav: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mSearchField: EditText? = null
    private var mSearchField2: EditText? = null
    private var lastFirstVisiblePosition: Int = -1
    private var searchBack: ImageView? = null
    private var searchCancel: ImageView? = null
    private var mFilter: ImageView? = null
    private var imm: InputMethodManager? = null

    private var adapter: Featured? = null

    private var adLoader: AdLoader? = null
    private var mNativeAds = ArrayList<UnifiedNativeAd?>()
    private var mRecyclerViewItems = ArrayList<Any?>()
    private var AD_UNIT_ID: String = "ca-app-pub-5242580240582734/1788502852"

    private val mWaitHandler = Handler()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x: View = inflater.inflate(R.layout.fragment_featured, container, false)


        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("Featured")
        mDatabase!!.keepSynced(true)
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            mDatabaseFav = FirebaseDatabase.getInstance().reference.child("Favourites").child(mAuth!!.currentUser!!.uid)
            mDatabaseFav!!.keepSynced(true)

        }

        mRecyclerView = x.findViewById(R.id.featured_recycler_view) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this@FeaturedFragment.context)

        mDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecyclerViewItems.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    val p = dataSnapshot1.getValue(FeaturedAdapter::class.java)
                    mRecyclerViewItems.add(p!!)
                }
                adapter = Featured(this@FeaturedFragment.context!!, mRecyclerViewItems)
                mRecyclerView!!.adapter = adapter
                mWaitHandler.postDelayed({
                    try {
                        loadNativeAds()
                    } catch (ignored: Exception) {
                        ignored.printStackTrace()
                    }
                },5000)//5 seconds delay
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        mSearchField = x.findViewById(R.id.edit_text_search) as EditText
        mSearchField2 = x.findViewById(R.id.edit_text_search2) as EditText
        searchBack = x.findViewById(R.id.search_back)
        searchCancel = x.findViewById(R.id.search_cancel)
        mFilter = x.findViewById(R.id.filter)

        mSearchField!!.setOnClickListener {
            mSearchField!!.isVisible = false
            mFilter!!.isVisible = false
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
            mFilter!!.isVisible = true
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }

        mFilter!!.setOnClickListener(this)

        return x
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

            val currentUser = mAuth!!.currentUser
            if (currentUser != null) {
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
                    .networkPolicy(NetworkPolicy.OFFLINE)//fetches cached images from disk
                    .into(songCover, object : Callback {
                        //loads pic to image view
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()//on cache cleared or error this ensures re-download of images
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

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FeaturedAdapter, LyricsViewHolder>(FeaturedAdapter::class.java, R.layout.layout_listitem, LyricsViewHolder::class.java, query) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: FeaturedAdapter, position: Int) {

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                viewHolder.favourties()
                viewHolder.setFavBtn(lyricsKey)

                viewHolder.mFavBtn!!.setOnClickListener {
                    mProcessFav = true

                    val currentUser = mAuth!!.currentUser
                    if (currentUser != null) {
                        mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (mProcessFav) {

                                    if (dataSnapshot.hasChild(lyricsKey)) {

                                        mDatabaseFav!!.child(lyricsKey).removeValue()
                                        mProcessFav = false
                                    } else {
                                        mDatabaseFav!!.child(lyricsKey).setValue(getItem(position))
                                        mProcessFav = false
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    } else {
                        Toasty.normal(this@FeaturedFragment.context!!, "Sign In to add Favourite").show()
                    }

                }
                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@FeaturedFragment.context, LyricsActivity::class.java)
                    lyricsIntent.putExtra("Lyrics_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                    onStart()
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
            mFilter!!.isVisible = true
            imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
        }

    }

    override fun onPause() {
        super.onPause()
        lastFirstVisiblePosition = (mRecyclerView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()//stores last position on pause
    }

    private fun showMenu(v: View) {
        PopupMenu(this@FeaturedFragment.context!!, v).apply {
            // Profile Fragment implements OnMenuItemClickListener
            setOnMenuItemClickListener(this@FeaturedFragment)
            inflate(R.menu.filter)
            show()
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.filter -> {
                showMenu(view)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.afro_pop -> {
                afroPop()
                true
            }
            R.id.gospel -> {
                gospel()
                true
            }
            R.id.hip_hop -> {
                hipHop()
                true
            }
            R.id.r_b -> {
                rAndB()
                true
            }
            R.id.reset -> {
                mDatabase!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mRecyclerViewItems.clear()
                        for (dataSnapshot1 in dataSnapshot.children) {
                            val p = dataSnapshot1.getValue(FeaturedAdapter::class.java)
                            mRecyclerViewItems.add(p!!)
                        }
                        adapter = Featured(this@FeaturedFragment.context!!, mRecyclerViewItems)
                        mRecyclerView!!.adapter = adapter
                        mWaitHandler.postDelayed({
                            try {
                                loadNativeAds()
                            } catch (ignored: Exception) {
                                ignored.printStackTrace()
                            }
                        },5000)//5 seconds delay
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
                true
            }
            else -> false
        }
    }

    private fun isNetworkAvailable(): Boolean {//check if internet connection exists
        val connectivityManager = getActivity()!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }//if exists == true else == false

    private fun insertAdsInEntItems() {
        if (mNativeAds.size <= 0) {
            return
        }
        val offset = mRecyclerViewItems.size / mNativeAds.size + 1
        var index = 3
        for (ad in mNativeAds) {
            mRecyclerViewItems.add(index, ad)
            index = index + offset
        }
    }

    private fun loadNativeAds(){
        if (isNetworkAvailable()){
            val builder = AdLoader.Builder(this@FeaturedFragment.context!!, AD_UNIT_ID)
            adLoader = builder.forUnifiedNativeAd(
                    object : UnifiedNativeAd.OnUnifiedNativeAdLoadedListener {
                        override fun onUnifiedNativeAdLoaded(unifiedNativeAd: UnifiedNativeAd?) {
                            // A native ad loaded successfully, check if the ad loader has finished loading
                            // and if so, insert the ads into the list.
                            mNativeAds.add(unifiedNativeAd)
                            if (!adLoader!!.isLoading()) {
                                insertAdsInEntItems()
                            }
                        }
                    }).withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            // A native ad failed to load, check if the ad loader has finished loading
                            // and if so, insert the ads into the list.
                            Log.e("MainActivity", ("The previous native ad failed to load. Attempting to" + " load another."))
                            if (!adLoader!!.isLoading()) {
                                insertAdsInEntItems()
                            }
                        }
                    }).build()
            adLoader!!.loadAds(AdRequest.Builder().build(), 5)
        }else{
        }
    }

    private fun afroPop() {
        val mDatabaseAfro = mDatabase!!.orderByChild("genre").equalTo("afro pop")
        mDatabaseAfro.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecyclerViewItems.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    val p = dataSnapshot1.getValue(FeaturedAdapter::class.java)
                    mRecyclerViewItems.add(p!!)
                }
                adapter = Featured(this@FeaturedFragment.context!!, mRecyclerViewItems)
                mRecyclerView!!.adapter = adapter
                mWaitHandler.postDelayed({
                    try {
                        loadNativeAds()
                    } catch (ignored: Exception) {
                        ignored.printStackTrace()
                    }
                },5000)//5 seconds delay
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun gospel() {
        val query = mDatabase!!.orderByChild("genre").equalTo("gospel")
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FeaturedAdapter, LyricsViewHolder>(FeaturedAdapter::class.java, R.layout.layout_listitem, LyricsViewHolder::class.java, query) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: FeaturedAdapter, position: Int) {

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                viewHolder.favourties()
                viewHolder.setFavBtn(lyricsKey)

                viewHolder.mFavBtn!!.setOnClickListener {
                    mProcessFav = true

                    val currentUser = mAuth!!.currentUser
                    if (currentUser != null) {
                        mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (mProcessFav) {

                                    if (dataSnapshot.hasChild(lyricsKey)) {

                                        mDatabaseFav!!.child(lyricsKey).removeValue()
                                        mProcessFav = false
                                    } else {
                                        mDatabaseFav!!.child(lyricsKey).setValue(getItem(position))
                                        mProcessFav = false
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    } else {
                        Toasty.normal(this@FeaturedFragment.context!!, "Sign In to add Favourite").show()
                    }
                }
                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@FeaturedFragment.context, LyricsActivity::class.java)
                    lyricsIntent.putExtra("Lyrics_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter
    }

    private fun hipHop() {
        val query = mDatabase!!.orderByChild("genre").equalTo("hip hop")
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FeaturedAdapter, LyricsViewHolder>(FeaturedAdapter::class.java, R.layout.layout_listitem, LyricsViewHolder::class.java, query) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: FeaturedAdapter, position: Int) {

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                viewHolder.favourties()
                viewHolder.setFavBtn(lyricsKey)

                viewHolder.mFavBtn!!.setOnClickListener {
                    mProcessFav = true

                    val currentUser = mAuth!!.currentUser
                    if (currentUser != null) {
                        mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (mProcessFav) {

                                    if (dataSnapshot.hasChild(lyricsKey)) {

                                        mDatabaseFav!!.child(lyricsKey).removeValue()
                                        mProcessFav = false
                                    } else {
                                        mDatabaseFav!!.child(lyricsKey).setValue(getItem(position))
                                        mProcessFav = false
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    } else {
                        Toasty.normal(this@FeaturedFragment.context!!, "Sign In to add Favourite").show()
                    }
                }
                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@FeaturedFragment.context, LyricsActivity::class.java)
                    lyricsIntent.putExtra("Lyrics_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter
    }

    private fun rAndB() {
        val query = mDatabase!!.orderByChild("genre").equalTo("R&B")
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FeaturedAdapter, LyricsViewHolder>(FeaturedAdapter::class.java, R.layout.layout_listitem, LyricsViewHolder::class.java, query) {
            override fun populateViewHolder(viewHolder: LyricsViewHolder, model: FeaturedAdapter, position: Int) {

                val lyricsKey = getRef(position).key.toString()
                viewHolder.setArtist(model.artist!!)
                viewHolder.setGenre(model.genre!!)
                viewHolder.setSong(model.song!!)
                viewHolder.setCover(activity!!.applicationContext, model.cover!!)
                viewHolder.favourties()
                viewHolder.setFavBtn(lyricsKey)

                viewHolder.mFavBtn!!.setOnClickListener {
                    mProcessFav = true

                    val currentUser = mAuth!!.currentUser
                    if (currentUser != null) {
                        mDatabaseFav!!.addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (mProcessFav) {

                                    if (dataSnapshot.hasChild(lyricsKey)) {

                                        mDatabaseFav!!.child(lyricsKey).removeValue()
                                        mProcessFav = false
                                    } else {
                                        mDatabaseFav!!.child(lyricsKey).setValue(getItem(position))
                                        mProcessFav = false
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    } else {
                        Toasty.normal(this@FeaturedFragment.context!!, "Sign In to add Favourite").show()
                    }
                }
                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val lyricsIntent = Intent(this@FeaturedFragment.context, LyricsActivity::class.java)
                    lyricsIntent.putExtra("Lyrics_id", lyricsKey)
                    startActivity(lyricsIntent)

                    mSearchField2!!.clearFocus()
                    mSearchField2!!.text.clear()
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter
    }
}
