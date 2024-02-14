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
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import kotlin.collections.ArrayList

class Hot(context: Context, recyclerViewItems: ArrayList<Any?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRecyclerViewItems: ArrayList<Any?> = recyclerViewItems
    private var mDatabase: DatabaseReference? = null
    private var mContext: Context? = context
    private val TYPE_HOT: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val hotItemLayoutView = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_listitem_hot, parent, false)
        return HotViewHolder(hotItemLayoutView)
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_HOT
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_HOT) {
            val hotItem: HotAdapter = mRecyclerViewItems[position] as HotAdapter
            (holder as HotViewHolder).artist!!.text = hotItem.artist
            holder.song!!.text = hotItem.song
            Picasso.get()
                    .load(hotItem.cover)
                    .networkPolicy(NetworkPolicy.OFFLINE)//fetches cached images from disk
                    .into(holder.cover, object : Callback {
                        //loads pic to image view
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                            //Try again online if cache failed
                            Picasso.get()//on cache cleared or error this ensures re-download of images
                                    .load(hotItem.cover)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.ic_error_outline_white_48dp)
                                    .into(holder.cover, object : Callback {
                                        override fun onSuccess() {
                                        }

                                        override fun onError(e: Exception?) {
                                        }
                                    })
                        }
                    })
            mDatabase = FirebaseDatabase.getInstance().reference.child("Hot")
            mDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val song = childSnapshot.child("song").value.toString()
                        if (song == holder.song!!.text) {
                            run {
                                val lyricsKey = childSnapshot.ref.key.toString()
                                holder.itemView.setOnClickListener { v ->
                                    val context = v.context
                                    val intent = Intent(context, HotActivity::class.java)
                                    intent.putExtra("Hot_id", lyricsKey)
                                    context.startActivity(intent)
                                    //Toasty.normal(this@Hot.mContext!!, lyricsKey).show()
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

    class HotViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var artist: TextView? = null
        var song: TextView? = null
        var cover: CircleImageView? = null

        init {
            artist = view.findViewById(R.id.hot_artist) as TextView
            song = view.findViewById(R.id.hot_song_title) as TextView
            cover = view.findViewById(R.id.hot_music_cover) as CircleImageView
        }
    }
}
