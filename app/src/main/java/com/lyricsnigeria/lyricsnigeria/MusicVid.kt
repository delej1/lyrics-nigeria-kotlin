package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.firebase.database.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MusicVid(context: Context, recyclerViewItems: ArrayList<Any?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRecyclerViewItems: ArrayList<Any?> = recyclerViewItems
    private var mDatabase: DatabaseReference? = null
    private var mContext: Context? = context
    private val TYPE_MVID: Int = 0
    private val TYPE_AD: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_MVID) {
            val mvidItemLayoutView = LayoutInflater.from(parent.context).inflate(
                    R.layout.video_listitem, parent, false) as YouTubePlayerView
            MusicVidFragment().lifecycle.addObserver(mvidItemLayoutView)
            return VideoAdapter.VideoViewHolder(mvidItemLayoutView)
        } else {
            val unifiedNativeLayoutView = LayoutInflater.from(
                    parent.context).inflate(R.layout.vid_ad_list_item,
                    parent, false)
            return UnifiedNativeAdViewHolder(unifiedNativeLayoutView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val recyclerViewItem = mRecyclerViewItems.get(position)
        if (recyclerViewItem is VideoAdapter) {
            return TYPE_MVID
        }
        return TYPE_AD
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_AD) {
            val nativeAd: UnifiedNativeAd = mRecyclerViewItems[position] as UnifiedNativeAd
            populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).getAdView())
        } else if (viewType == TYPE_MVID) {
            val mvidItem: VideoAdapter = mRecyclerViewItems[position] as VideoAdapter
            (holder as VideoAdapter.VideoViewHolder).vidTitle!!.text = mvidItem.title
            mDatabase = FirebaseDatabase.getInstance().reference.child("Videos").child("Music")
            mDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val title = childSnapshot.child("title").value.toString()
                        if (title == holder.vidTitle!!.text) {
                            run {
                                holder.cueVideo(mvidItem.videoId!!)
                            }
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }


    override fun getItemCount(): Int {
        return mRecyclerViewItems.size
    }


    class VideoAdapter {
        var videoId: String? = null
        var title: String? = null

        constructor(videoId: String, title: String) {
            this.videoId = videoId
            this.title = title
        }
        constructor() {}

        class VideoViewHolder(playerView: YouTubePlayerView) : RecyclerView.ViewHolder(playerView) {
            private val youTubePlayerView: YouTubePlayerView = playerView
            private var youTubePlayer: YouTubePlayer? = null
            private var currentVideoId: String? = null
            var vidTitle: TextView? = null

            init {
                youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        this@VideoViewHolder.youTubePlayer = youTubePlayer
                        this@VideoViewHolder.youTubePlayer!!.cueVideo(currentVideoId!!, 0F)
                    }
                })
                vidTitle = youTubePlayerView.findViewById(R.id.video_title) as TextView
            }

            fun cueVideo(videoId: String) {
                currentVideoId = videoId
                if (youTubePlayer == null)
                    return
                youTubePlayer!!.cueVideo(videoId, 0F)
            }
        }
    }

    class UnifiedNativeAdViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        private var adView: UnifiedNativeAdView? = null

        fun getAdView(): UnifiedNativeAdView {
            return adView!!
        }

        init {
            adView = view.findViewById(R.id.vid_ad_view) as UnifiedNativeAdView
            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            adView!!.mediaView = adView!!.findViewById(R.id.vid_ad_media) as MediaView
            // Register the view used for each individual asset.
            adView!!.headlineView = adView!!.findViewById(R.id.vid_ad_headline)
            adView!!.bodyView = adView!!.findViewById(R.id.vid_ad_body)
            adView!!.callToActionView = adView!!.findViewById(R.id.vid_ad_call_to_action)
            adView!!.iconView = adView!!.findViewById(R.id.vid_ad_icon)
            adView!!.priceView = adView!!.findViewById(R.id.vid_ad_price)
            adView!!.starRatingView = adView!!.findViewById(R.id.vid_ad_stars)
            adView!!.storeView = adView!!.findViewById(R.id.vid_ad_store)
            adView!!.advertiserView = adView!!.findViewById(R.id.vid_ad_advertiser)
        }
    }

    private fun populateNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {

        // Some assets are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        val icon = nativeAd.icon
        if (icon == null) {
            adView.iconView.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd)
    }
}