package com.lyricsnigeria.lyricsnigeria

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

class TermsDialogue : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Terms & Conditions")
                .setMessage(R.string.djhs_eula)
                .setPositiveButton("Close") { dialogInterface, i -> }
        return builder.create()
    }
}
