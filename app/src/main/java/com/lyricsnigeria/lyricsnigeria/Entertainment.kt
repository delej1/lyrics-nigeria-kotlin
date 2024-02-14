package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception

class Entertainment(context: Context, recyclerViewItems: ArrayList<Any?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRecyclerViewItems: ArrayList<Any?> = recyclerViewItems
    private var mDatabase: DatabaseReference? = null
    private var mContext: Context? = context
    private val TYPE_ENT: Int = 0
    private val TYPE_AD: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ENT) {
            val entItemLayoutView = LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_listitem_ent, parent, false)
            return EntViewHolder(entItemLayoutView)
        } else {
            val unifiedNativeLayoutView = LayoutInflater.from(
                    parent.context).inflate(R.layout.ad_list_item,
                    parent, false)
            return UnifiedNativeAdViewHolder(unifiedNativeLayoutView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val recyclerViewItem = mRecyclerViewItems.get(position)
        if (recyclerViewItem is EntertainmentAdapter) {
            return TYPE_ENT
        }
        return TYPE_AD
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_AD) {
            val nativeAd: UnifiedNativeAd = mRecyclerViewItems[position] as UnifiedNativeAd
            populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).getAdView())
        } else if (viewType == TYPE_ENT) {
            val entItem: EntertainmentAdapter = mRecyclerViewItems[position] as EntertainmentAdapter
            (holder as EntViewHolder).entHeadline!!.text = entItem.headline
            holder.entContent!!.text = entItem.body
            Picasso.get()
                    .load(entItem.cover)
                    .networkPolicy(NetworkPolicy.OFFLINE)//fetches cached images from disk
                    .into(holder.entCover, object : Callback {
                        //loads pic to image view
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()//on cache cleared or error this ensures re-download of images
                                    .load(entItem.cover)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.ic_error_outline_white_48dp)
                                    .into(holder.entCover, object : Callback {
                                        override fun onSuccess() {
                                        }

                                        override fun onError(e: Exception?) {
                                        }
                                    })
                        }
                    })
            mDatabase = FirebaseDatabase.getInstance().reference.child("Gist")
            mDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val headline = childSnapshot.child("headline").value.toString()
                        if (headline == holder.entHeadline!!.text) {
                            run {
                                val lyricsKey = childSnapshot.ref.key.toString()
                                holder.itemView.setOnClickListener { v ->
                                    val context = v.context
                                    val intent = Intent(context, GistActivity::class.java)
                                    intent.putExtra("Gist_id", lyricsKey)
                                    context.startActivity(intent)
                                    //Toasty.normal(this@Entertainment.mContext!!, lyricsKey).show()
                                }
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


    class EntViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var entHeadline: TextView? = null
        var entContent: TextView? = null
        var entCover: ImageView? = null

        init {
            entHeadline = view.findViewById(R.id.headline) as TextView
            entContent = view.findViewById(R.id.content) as TextView
            entCover = view.findViewById(R.id.ent_cover) as ImageView
        }
    }

    class UnifiedNativeAdViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        private var adView: UnifiedNativeAdView? = null

        fun getAdView(): UnifiedNativeAdView {
            return adView!!
        }

        init {
            adView = view.findViewById(R.id.ad_view) as UnifiedNativeAdView
            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            //adView.setMediaView(adView.findViewById(R.id.ad_media) as MediaView)
            // Register the view used for each individual asset.
            adView!!.headlineView = adView!!.findViewById(R.id.ad_headline)
            adView!!.bodyView = adView!!.findViewById(R.id.ad_body)
            adView!!.callToActionView = adView!!.findViewById(R.id.ad_call_to_action)
            adView!!.iconView = adView!!.findViewById(R.id.ad_icon)
            adView!!.priceView = adView!!.findViewById(R.id.ad_price)
            adView!!.starRatingView = adView!!.findViewById(R.id.ad_stars)
            adView!!.storeView = adView!!.findViewById(R.id.ad_store)
            adView!!.advertiserView = adView!!.findViewById(R.id.ad_advertiser)
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