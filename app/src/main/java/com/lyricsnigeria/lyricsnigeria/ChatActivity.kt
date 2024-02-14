package com.lyricsnigeria.lyricsnigeria

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import es.dmoral.toasty.Toasty


class ChatActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mChatKey: String? = null
    private var mDatabase: DatabaseReference? = null
    private var mMsgRef: DatabaseReference? = null
    private var databaseRef: DatabaseReference? = null

    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private var inputMsg: EditText? = null
    private var sendMsgBtn: ImageView? = null

    private var text: TextView? = null


    var context: Context? = null
    val MESSAGE_IN_VIEW_TYPE = 1
    val MESSAGE_OUT_VIEW_TYPE = 2

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
        } else {
            if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME) {
                setTheme(R.style.NightTheme)
            } else {
                setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_chat)

        mChatKey = intent.extras!!.getString("Chat_id")

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("Chat").child(mChatKey!!).child("messages")
        databaseRef = FirebaseDatabase.getInstance().reference.child("Chat").child(mChatKey!!)

        mDatabase!!.keepSynced(true)
        databaseRef!!.keepSynced(true)

        val toolbar: Toolbar = findViewById(R.id.chat_toolbar) //make toolbar value and get xml toolbar id

        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }//set back button on toolbar

        text = findViewById(R.id.send_message)

        databaseRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mTitle = dataSnapshot.child("topic").value.toString()
                toolbar.title = mTitle
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

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

        inputMsg = findViewById(R.id.chat_msg_edit)
        sendMsgBtn = findViewById(R.id.chat_send_btn)


        mRecyclerView = findViewById(R.id.chat_recycler_view)
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.setItemViewCacheSize(20)

        mLayoutManager = LinearLayoutManager(this)
        mLayoutManager!!.stackFromEnd = true

        mRecyclerView!!.layoutManager = mLayoutManager

        displayMessage()

        sendMsgBtn!!.setOnClickListener {
            val currentUser = mAuth!!.currentUser
            if (currentUser != null) {
                if (inputMsg!!.text.toString().trim().isNotEmpty()) {
                    saveMsgToDatabase()
                    inputMsg!!.text = null
                    val itemCount = mRecyclerView!!.adapter!!.itemCount
                    mLayoutManager!!.smoothScrollToPosition(mRecyclerView, null, itemCount)
                } else {
                    Toasty.normal(this@ChatActivity, "Enter a Message").show()
                }
            } else {
                Toasty.normal(this@ChatActivity, "Sign In to Send Message").show()
            }
        }
    }

    private fun saveMsgToDatabase() {
        val name: String = mAuth!!.currentUser!!.displayName!!
        val message = inputMsg!!.text.toString()
        val dateTime = SimpleDateFormat("MMM dd, hh:mma", Locale.getDefault()).format(Date())

        mDatabase!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val msgCount = dataSnapshot.children.count()
                databaseRef!!.child("count").setValue(msgCount)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

        val allMessageKey: HashMap<String, Any> = HashMap()
        mDatabase!!.updateChildren(allMessageKey)
        mMsgRef = mDatabase!!.push()

        val msgInfoMap: HashMap<String, Any> = HashMap()
        msgInfoMap["name"] = name
        msgInfoMap["message"] = message
        msgInfoMap["time"] = dateTime
        mMsgRef!!.updateChildren(msgInfoMap)
    }

    private fun displayMessage() {

        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<MessageAdapter, ChatViewHolder>(MessageAdapter::class.java, R.layout.layout_listitem_inmsg_chat, ChatViewHolder::class.java, mDatabase) {

                override fun getItemViewType(position: Int): Int {
                    if (getItem(position).name.equals(mAuth!!.currentUser!!.displayName)) {
                        return MESSAGE_OUT_VIEW_TYPE
                    }
                    return MESSAGE_IN_VIEW_TYPE
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                    val view: View?
                    if (viewType == MESSAGE_IN_VIEW_TYPE) {
                        view = LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_listitem_inmsg_chat, parent, false)
                    } else {
                        view = LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_listitem_outmsg_chat, parent, false)
                    }
                    return ChatViewHolder(view)
                }

                override fun populateViewHolder(viewHolder: ChatViewHolder, model: MessageAdapter, position: Int) {

                    viewHolder.setName(model.name!!)
                    viewHolder.setMessage(model.message!!)
                    viewHolder.setTime(model.time!!)
                }
            }
            mRecyclerView!!.adapter = firebaseRecyclerAdapter
            mDatabase!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {//add listener for child add event
                    val itemCount = mRecyclerView!!.adapter!!.itemCount
                    mLayoutManager!!.smoothScrollToPosition(mRecyclerView, null, itemCount)//scroll layout to last message on child added
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        } else {
            val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<MessageAdapter, ChatViewHolder>(MessageAdapter::class.java, R.layout.layout_listitem_inmsg_chat, ChatViewHolder::class.java, mDatabase) {

                override fun getItemViewType(position: Int): Int {
                    return MESSAGE_IN_VIEW_TYPE
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                    val view: View? = LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_listitem_inmsg_chat, parent, false)
                    return ChatViewHolder(view!!)
                }

                override fun populateViewHolder(viewHolder: ChatViewHolder, model: MessageAdapter, position: Int) {

                    viewHolder.setName(model.name!!)
                    viewHolder.setMessage(model.message!!)
                    viewHolder.setTime(model.time!!)
                }
            }
            mRecyclerView!!.adapter = firebaseRecyclerAdapter
            mDatabase!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {//add listener for child add event
                    val itemCount = mRecyclerView!!.adapter!!.itemCount
                    mLayoutManager!!.smoothScrollToPosition(mRecyclerView, null, itemCount)//scroll layout to last message on child added
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

    }

    class ChatViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        fun setName(name: String) {
            val userName = mView.findViewById<View>(R.id.user_display_text) as TextView
            userName.text = name
        }

        fun setMessage(message: String) {
            val userMessage = mView.findViewById<View>(R.id.msg_display_text) as TextView
            userMessage.text = message
        }

        fun setTime(time: String) {
            val msgTime = mView.findViewById<View>(R.id.time_display_text) as TextView
            msgTime.text = time
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }//implement back button on toolbar
}



