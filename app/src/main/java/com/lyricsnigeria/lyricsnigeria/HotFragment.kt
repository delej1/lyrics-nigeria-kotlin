package com.lyricsnigeria.lyricsnigeria


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.collections.ArrayList


class HotFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    private var lastFirstVisiblePosition: Int = -1

    private var adapter: Hot? = null

    private val mRecyclerViewItems = ArrayList<Any?>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x: View = inflater.inflate(R.layout.fragment_hot, container, false)


        mDatabase = FirebaseDatabase.getInstance().reference.child("Hot")

        mDatabase!!.keepSynced(true)

        mRecyclerView = x.findViewById(R.id.hot_recycler_view) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this@HotFragment.context)


        mDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecyclerViewItems.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    val p = dataSnapshot1.getValue(HotAdapter::class.java)
                    mRecyclerViewItems.add(p!!)
                }
                adapter = Hot(this@HotFragment.context!!, mRecyclerViewItems)
                mRecyclerView!!.adapter = adapter
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
}
