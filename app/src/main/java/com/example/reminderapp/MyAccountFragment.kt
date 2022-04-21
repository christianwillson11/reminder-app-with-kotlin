package com.example.reminderapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [MyAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyAccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var scheduleCount = 0

    private val db = Firebase.firestore

    lateinit var profileName: TextView
    lateinit var schedulesCount: TextView
    lateinit var friendsCount: TextView
    lateinit var invitationsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_account, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileName = view.findViewById(R.id.profileName)
        val userId = view.findViewById<TextView>(R.id.userId)
        GlobalScope.launch(Dispatchers.IO) {
            val docRef = db.collection("users").document(Functions.loggedInUser!!.uid)
            val document = docRef.get().await()
            withContext(Dispatchers.Main) {
                profileName.text = document.data?.get("full_name").toString()
                userId.text = "UID: ${Functions.loggedInUser.uid}"
                view.findViewById<TextView>(R.id.tvInitial).text = document.data?.get("full_name").toString().take(1)
            }
        }

        schedulesCount = view.findViewById(R.id.tvSchedulesCount)
        friendsCount = view.findViewById(R.id.tvFriendsCount)
        invitationsCount = view.findViewById(R.id.tvInvitationsCount)

        countSchedule()
        countFriends()

        val btnChangeUsername = view.findViewById<Button>(R.id.changeUsernameBtn)
        btnChangeUsername.setOnClickListener {
            showDialog(R.layout.change_username_dialog, "Change Username")
        }

        val btnChangeFullName = view.findViewById<Button>(R.id.changeFullNameBtn)
        btnChangeFullName.setOnClickListener {
            showDialog(R.layout.change_full_name_dialog, "Change Full Name")
        }

        val btnLogout = view.findViewById<Button>(R.id.logoutBtn)

        btnLogout.setOnClickListener {
            removeTokenAndLogout()
        }

    }

    private fun performChangeUsername(new: String) {

        db.collection("users").whereEqualTo("username", new).get()
            .addOnSuccessListener { documents ->
                var co = 0
                for (document in documents) {
                    co++
                }
                
                if (co == 0) {
                    db.collection("users").document(Functions.loggedInUser!!.uid)
                        .update("username", new)
                        .addOnSuccessListener {
                            SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Yeay!")
                                .setContentText("You have successfully update your username!")
                                .setConfirmText("OK")
                                .show()
                        }
                        .addOnFailureListener { exception ->
                            SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops! Something went wrong")
                                .setContentText("Error: $exception")
                                .setConfirmText("OK")
                                .show()

                        }
                } else {
                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Cannot Change Username")
                        .setContentText("That username already taken")
                        .setConfirmText("OK")
                        .show()
                }
            }


    }

    private fun performChangeFullName(new: String) {
        db.collection("users").document(Functions.loggedInUser!!.uid)
            .update("full_name", new)
            .addOnSuccessListener {
                SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Yeay!")
                    .setContentText("You have successfully update your name!")
                    .setConfirmText("OK")
                    .show()
                profileName.text = new
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops! Something went wrong")
                    .setContentText("Error: $exception")
                    .setConfirmText("OK")
                    .show()

            }
    }

    private fun showDialog(dialog: Int, title: String) {
        val mDialogView = LayoutInflater.from(activity).inflate(dialog, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
            .setTitle(title)
        val mAlertDialog = mBuilder.show()
        var new: String
        mDialogView.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            if (dialog == R.layout.change_full_name_dialog) {
                new = mDialogView.findViewById<EditText>(R.id.tvNewFullName).text.toString()
                performChangeFullName(new)
            } else {
                new = mDialogView.findViewById<EditText>(R.id.tvNewUsername).text.toString()
                performChangeUsername(new)
            }
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun countSchedule() {
        db.collection("schedule")
            .whereEqualTo("initiator", Functions.loggedInUser!!.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for (document in documents) {
                        scheduleCount++
                    }
                }
                schedulesCount.text = scheduleCount.toString()
            }
            .addOnFailureListener {
                SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("ERROR!")
                    .setContentText("Fail to fetch schedule data")
                    .setConfirmText("Ok")
                    .show()
            }
    }

    private fun countFriends() {
        db.collection("users").document(Functions.loggedInUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                var f = document.get("friends").toString()
                var i = document.get("invitations").toString()
                if (f != "null") {
                    f = Functions.SanitizeString(f)
                    val tmp = f.split(" ").toTypedArray()

                    friendsCount.text = tmp.size.toString()

                } else {
                    friendsCount.text = "0"
                }

                if (i != "null" && i != "[]") {
                    i = Functions.SanitizeString(i)
                    val tmp2 = i.split(" ").toTypedArray()
                    invitationsCount.text = tmp2.size.toString()
                } else {
                    invitationsCount.text = "0"
                }

            }
            .addOnFailureListener {
                SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("ERROR!")
                    .setContentText("Fail to fetch schedule data")
                    .setConfirmText("Ok")
                    .show()
            }
    }

    private fun removeTokenAndLogout() {
        db.collection("users").document(Functions.loggedInUser!!.uid)
            .update("token", "")
            .addOnSuccessListener {
                Firebase.auth.signOut()
                val eIntent = Intent(activity, MainActivity::class.java)
                startActivity(eIntent)
            }
            .addOnFailureListener {
                SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("ERROR!")
                    .setContentText("Please Try Again")
                    .setConfirmText("Ok")
                    .show()
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyAccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}