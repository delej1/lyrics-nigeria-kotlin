package com.lyricsnigeria.lyricsnigeria


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty

class RoomForm : DialogFragment() {

    private var editText1: EditText? = null
    private var editText2: EditText? = null
    private var listener: RoomFormListener? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.new_room_form, null)

        mAuth = FirebaseAuth.getInstance()

        editText1 = view.findViewById(R.id.editText1) as EditText
        editText2 = view.findViewById(R.id.editText2) as EditText



        builder.setView(view)
                .setNegativeButton("CANCEL") { dialogInterface, i -> }

                .setPositiveButton("SUBMIT") { dialogInterface, i ->


                    if (editText1!!.text.toString().trim().isNotEmpty() && editText2!!.text.toString().trim().isNotEmpty()) {
                        val category = editText1!!.text.toString()
                        val topic = editText2!!.text.toString()

                        val mMsgRef = FirebaseDatabase.getInstance().reference.child("Room Requests").child(mAuth!!.currentUser!!.uid).push()
                        val msgInfoMap: HashMap<String, Any> = HashMap()
                        msgInfoMap["category"] = category
                        msgInfoMap["topic"] = topic
                        mMsgRef.updateChildren(msgInfoMap)
                        Toasty.normal(this@RoomForm.context!!, "Awaiting Approval").show()
                    } else {
                        Toasty.normal(this@RoomForm.context!!, "Fill all Fields").show()
                    }
                }
        return builder.create()
    }


    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        try {
            listener = context as RoomFormListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " Must Implement RoomFormListener"))
        }
    }


    interface RoomFormListener {
        fun applyTexts(category: String, topic: String)
    }

}