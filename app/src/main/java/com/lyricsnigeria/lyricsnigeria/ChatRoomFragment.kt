package com.lyricsnigeria.lyricsnigeria


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty


class ChatRoomFragment : Fragment(), View.OnClickListener, RoomForm.RoomFormListener {

    private var mRecyclerView: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var lastFirstVisiblePosition: Int = -1

    private var newRoom: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val x = inflater.inflate(R.layout.fragment_chat_room, container, false)

        mAuth = FirebaseAuth.getInstance()

        mDatabase = FirebaseDatabase.getInstance().reference.child("Chat")
        mDatabase!!.keepSynced(true)

        mRecyclerView = x.findViewById(R.id.chat_recycler_view) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this@ChatRoomFragment.context)

        newRoom = x.findViewById(R.id.btn_add_room) as ImageView
        newRoom!!.bringToFront()
        newRoom!!.setOnClickListener(this)

        loadChatRooms()

        return x
    }

    private fun loadChatRooms(){
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatAdapter, ChatViewHolder>(ChatAdapter::class.java, R.layout.layout_listitem_chat, ChatViewHolder::class.java, mDatabase) {
            override fun populateViewHolder(viewHolder: ChatViewHolder, model: ChatAdapter, position: Int) {//populating a viewHolder (RecyclerView)

                val chatKey = getRef(position).key.toString()
                viewHolder.setCategory(model.category!!)
                viewHolder.setTopic(model.topic!!)
                viewHolder.setCount(model.count!!)


                mDatabase!!.child(chatKey).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {//add listener for child add event
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                        val notification = viewHolder.mView.findViewById(R.id.notification_dot) as ImageView
                        notification.isVisible = true
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

                //processing event for click on cardview
                viewHolder.mView.setOnClickListener {
                    val chatIntent = Intent(this@ChatRoomFragment.context, ChatActivity::class.java)
                    chatIntent.putExtra("Chat_id", chatKey)
                    startActivity(chatIntent)

                    val notification = viewHolder.mView.findViewById(R.id.notification_dot) as ImageView
                    notification.isVisible = false
                }
            }
        }
        mRecyclerView!!.adapter = firebaseRecyclerAdapter
    }

    class ChatViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
        fun setCategory(category: String) {
            val chatCat = mView.findViewById<View>(R.id.chat_category) as TextView
            chatCat.text = category
        }

        fun setTopic(topic: String) {
            val chatTop = mView.findViewById<View>(R.id.chat_topic) as TextView
            chatTop.text = topic
        }

        fun setCount(topic: Long) {
            val chatCount = mView.findViewById<View>(R.id.chat_count) as TextView
            chatCount.text = topic.toString()
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_add_room -> {
                val currentUser = mAuth!!.currentUser
                if (currentUser != null) {
                    openForm()
                }else{
                    Toasty.normal(this@ChatRoomFragment.context!!, "Sign In to add Room").show()
                }
            }
        }
    }

    private fun openForm() {
        val roomForm = RoomForm()
        roomForm.show(fragmentManager!!, "new room form")
        roomForm.isCancelable = false
    }

    override fun applyTexts(category: String, topic: String) {

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
