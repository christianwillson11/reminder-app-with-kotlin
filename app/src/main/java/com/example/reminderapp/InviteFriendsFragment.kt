package com.example.reminderapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var layoutManager: RecyclerView.LayoutManager? = null
private var adapter: RecyclerView.Adapter<FriendsListRecyclerAdapter.ViewHolder>? = null

/**
 * A simple [Fragment] subclass.
 * Use the [InviteFriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InviteFriendsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val db = Firebase.firestore

    private var pendingFriends = arrayListOf<FriendData>()

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
        return inflater.inflate(R.layout.fragment_invite_friends, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInvite = view.findViewById<EditText>(R.id.EditTextUsernameInvite)
        val btnInvite = view.findViewById<Button>(R.id.btnInvite)

        fetchDataFromDb()

        layoutManager = LinearLayoutManager(activity)
        val _friendsListRecyclerView = view.findViewById<RecyclerView>(R.id.friendsListRecyclerView)
        _friendsListRecyclerView.layoutManager = layoutManager
        adapter = FriendsListRecyclerAdapter(pendingFriends)
        _friendsListRecyclerView.adapter = adapter

        btnInvite.setOnClickListener {

            if (!usernameInvite.text.toString().isEmpty()) {
                val username = usernameInvite.text.toString()

                GlobalScope.launch(Dispatchers.IO) {
                    val userCollection = db.collection("users")
                    val friend_documents = userCollection.whereEqualTo("username", username)
                        .get()
                        .await()
                    val self_documents = userCollection.document(Functions.loggedInUser!!.uid)
                        .get()
                        .await()
                    if (friend_documents.first().data.toString().contains(Functions.loggedInUser!!.uid) || self_documents.data.toString().contains(friend_documents.first().id)) {
                        withContext(Dispatchers.Main) {
                            SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops!")
                                .setContentText("You have already invited this person")
                                .setConfirmText("OK")
                                .show()
                        }
                    } else {
                        db.collection("users").document(friend_documents.first().id).update("invitations", FieldValue.arrayUnion(Functions.loggedInUser!!.uid))
                        fetchDataFromDb()
                    }
                }
            } else {
                SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops!")
                    .setContentText("You have already invited this person")
                    .show()
            }

        }

    }

    private fun fetchDataFromDb() {
        pendingFriends.clear()
        val docRef = db.collection("users")
        docRef.whereArrayContains("invitations", Functions.loggedInUser!!.uid)
            .get()
            .addOnSuccessListener { result->
                for (document in result) {
                    pendingFriends.add(FriendData(R.drawable.ic_search, null, document.get("full_name").toString(), document.get("username").toString(), true))
                    adapter?.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception->
                Log.d("FirebaseMsg", exception.toString())
            }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InviteFriendsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InviteFriendsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}