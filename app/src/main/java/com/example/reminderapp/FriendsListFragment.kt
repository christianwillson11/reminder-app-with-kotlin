package com.example.reminderapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val db = Firebase.firestore

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<FriendsListRecyclerAdapter.ViewHolder>? = null

    private var friendsData = arrayListOf<FriendData>()

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
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loggedInUser = FirebaseAuth.getInstance().getCurrentUser()

        fetchFriendsDataFromFiresetore(loggedInUser!!.uid)


        layoutManager = LinearLayoutManager(activity)

        val _friendsListRecyclerView = view.findViewById<RecyclerView>(R.id.friendsListRecyclerView)
        _friendsListRecyclerView.layoutManager = layoutManager

        adapter = FriendsListRecyclerAdapter(friendsData)
        _friendsListRecyclerView.adapter = adapter

        (adapter as FriendsListRecyclerAdapter).setOnItemClickCallback(object: FriendsListRecyclerAdapter.OnItemClickCallback {
            override fun onItemClicked(data: FriendData, mode: Int) {
                val uid = data.id.toString()
                if (mode == 1) {
                    db.collection("users").document(uid).update("friends", FieldValue.arrayUnion(loggedInUser!!.uid))
                    db.collection("users").document(loggedInUser!!.uid).update("friends", FieldValue.arrayUnion(uid))
                    db.collection("users").document(loggedInUser!!.uid).update("invitations", FieldValue.arrayRemove(uid))

                    friendsData.clear()
                    fetchFriendsDataFromFiresetore(loggedInUser!!.uid)
                } else {

                    db.collection("users").document(loggedInUser!!.uid).update("invitations", FieldValue.arrayRemove(uid))

                }
            }
        })
    }


    fun fetchFriendsDataFromFiresetore(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document->
                var tmp = document.data!!.get("friends").toString()
                if (tmp != "[]" && tmp != "null") {
                    tmp = Functions.SanitizeString(tmp)
                    val friends = tmp.split(" ").toTypedArray()
                    for (uid in friends) {
                        getUserDetailFromId(uid, true)
                    }
                }

                var tmp2 = document.data!!.get("invitations").toString()
                if (tmp2 != "[]" && tmp2 != "null") {
                    tmp2 = Functions.SanitizeString(tmp2)
                    val friendRequests = tmp2.split(" ").toTypedArray()
                    for (uid in friendRequests) {
                        getUserDetailFromId(uid, false)
                    }
                }

            }
            .addOnFailureListener { exception->
                Log.w("FirebaseMsg", "Error getting documents: ", exception)
            }
    }


    fun getUserDetailFromId(uid: String, status: Boolean) {

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                friendsData.add(FriendData(R.drawable.profile_picture, uid, document.data!!.get("full_name").toString(), document.data!!.get("username").toString(), status))
                friendsData.sortBy { it.full_name }
                adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("FirebaseMsg", "Error getting documents: ", exception)
            }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}