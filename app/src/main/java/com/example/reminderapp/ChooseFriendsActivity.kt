package com.example.reminderapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChooseFriendsActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<FriendsListViewAdapter.ViewHolder>? = null
    private var dataModel = ArrayList<FriendData>()

    private var invitedFriendsId = arrayListOf<String>()
    private var invitedFriendsName = arrayListOf<String>()

    fun getUserDetailFromId(uid: String, status: Boolean) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                dataModel.add(FriendData(R.drawable.ic_search, uid, document.data!!.get("full_name").toString(), document.data!!.get("username").toString(), status))
                adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->

                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Error: $exception")
                    .setConfirmText("OK")
                    .show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_friends)

        val loggedInUser = FirebaseAuth.getInstance().getCurrentUser()

        val dataIntent = intent.getStringArrayListExtra("selection")
        if (dataIntent != null) {
            invitedFriendsId = dataIntent
            for (id in invitedFriendsId) {
                db.collection("users").document(id)
                    .get()
                    .addOnSuccessListener { document ->
                        invitedFriendsName.add(document.get("full_name").toString())
                    }
            }
        }


        db.collection("users").document(loggedInUser!!.uid).get()
            .addOnSuccessListener { document ->
                var tmp = document.get("friends").toString()

                if (tmp != "[]") {
                    tmp = Functions.SanitizeString(tmp)
                    val friends = tmp.split(" ").toTypedArray()
                    for (uid in friends) {
                        getUserDetailFromId(uid, true)
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseMsg", "Error adding document", exception)
            }



        layoutManager = LinearLayoutManager(this)

        val _friendsListRecyclerView = findViewById<RecyclerView>(R.id.chooseFriendsRecyclerView)
        _friendsListRecyclerView.layoutManager = layoutManager

        adapter = FriendsListViewAdapter(dataModel, invitedFriendsId)
        _friendsListRecyclerView.adapter = adapter

        (adapter as FriendsListViewAdapter).setOnItemClickCallback(object: FriendsListViewAdapter.OnItemClickCallback {
            override fun onItemClicked(data: List<String>, data2: List<String>) {
                invitedFriendsId = data as ArrayList<String>
                invitedFriendsName = data2 as ArrayList<String>
            }
        })

        val doneBtn = findViewById<Button>(R.id.btnDone)
        doneBtn.setOnClickListener {
            val eIntent = Intent(this@ChooseFriendsActivity, AddScheduleActivity::class.java).apply {
                putExtra(AddScheduleActivity.item_data, invitedFriendsId)
                putExtra(AddScheduleActivity.item_data2, invitedFriendsName)
            }
            setResult(Activity.RESULT_OK, eIntent)
            finish()
        }

    }
}