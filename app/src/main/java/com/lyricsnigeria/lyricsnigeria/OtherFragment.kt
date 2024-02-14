package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import java.lang.Exception

class OtherFragment : Fragment(){

    private var mRecyclerView: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var lastFirstVisiblePosition: Int = -1

    private var adapter: Other? = null

    private var adLoader: AdLoader? = null
    private var mNativeAds = ArrayList<UnifiedNativeAd?>()
    private var mRecyclerViewItems = ArrayList<Any?>()
    private var AD_UNIT_ID: String = "ca-app-pub-5242580240582734/1436330826"

    private val mWaitHandler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val x: View = inflater.inflate(R.layout.fragment_other, container, false)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("Other News")


        mDatabase!!.keepSynced(true)

        mRecyclerView = x.findViewById(R.id.other_gist_recycler_view) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this@OtherFragment.context)

        mDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecyclerViewItems.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    val p = dataSnapshot1.getValue(EntertainmentAdapter::class.java)
                    mRecyclerViewItems.add(p!!)
                }
                adapter = Other(this@OtherFragment.context!!, mRecyclerViewItems)
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


        return x
    }

    override fun onResume() {
        super.onResume()

        val vto = mRecyclerView!!.viewTreeObserver
        vto.addOnGlobalLayoutListener { (mRecyclerView!!.layoutManager as LinearLayoutManager).scrollToPosition(lastFirstVisiblePosition) }//scrolls to last position on resume

        Handler().postDelayed(Runnable { lastFirstVisiblePosition = -1 }, 200)//delays reset of lastFirstVisiblePosition by 200 milli seconds
    }

    override fun onPause() {
        super.onPause()
        lastFirstVisiblePosition = (mRecyclerView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()//stores last position on pause
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
            val builder = AdLoader.Builder(this@OtherFragment.context!!, AD_UNIT_ID)
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

