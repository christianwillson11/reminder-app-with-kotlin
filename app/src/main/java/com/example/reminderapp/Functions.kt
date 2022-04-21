package com.example.reminderapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Functions {

    companion object {
        val loggedInUser = FirebaseAuth.getInstance().getCurrentUser()

        fun SanitizeString(_str: String): String {
            var str = _str
            str = str.replace("[", "")
            str = str.replace("]", "")
            str = str.replace(",", "")
            return str
        }
    }


}