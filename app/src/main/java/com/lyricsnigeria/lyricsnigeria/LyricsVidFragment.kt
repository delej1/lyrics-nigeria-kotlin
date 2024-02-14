package com.lyricsnigeria.lyricsnigeria


import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.database.*
import java.lang.Exception


class LyricsVidFragment : Fragment() {

    private var mDatabase: DatabaseReference? = null
    private var recyclerView: RecyclerView? = null

    private var adapter: LyricsVid? = null

    private var adLoader: AdLoader? = null
    private var mNativeAds = ArrayList<UnifiedNativeAd?>()
    private var mRecyclerViewItems = ArrayList<Any?>()
    private var AD_UNIT_ID: String = "ca-app-pub-5242580240582734/6737237890"

    private val mWaitHandler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x = inflater.inflate(R.layout.fragment_lyrics_vid, container, false)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Videos").child("Lyrics")

        recyclerView = x.findViewById(R.id.lvid_recycler_view)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this@LyricsVidFragment.context)

        mDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecyclerViewItems.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    val p = dataSnapshot1.getValue(LyricsVid.VideoAdapter::class.java)
                    mRecyclerViewItems.add(p!!)
                }
                adapter = LyricsVid(this@LyricsVidFragment.context!!, mRecyclerViewItems)
                recyclerView!!.adapter = adapter
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
        return  x
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
        var index = 2
        for (ad in mNativeAds) {
            mRecyclerViewItems.add(index, ad)
            index = index + offset
        }
    }

    private fun loadNativeAds(){
        if (isNetworkAvailable()){
            val builder = AdLoader.Builder(this@LyricsVidFragment.context!!, AD_UNIT_ID)
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
}
