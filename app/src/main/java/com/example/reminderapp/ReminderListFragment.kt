package com.example.reminderapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderapp.databinding.FragmentReminderListBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReminderListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReminderListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<ReminderRecyclerAdapter.ViewHolder>? = null

    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore

    private var reminder_items = arrayListOf<Reminder>()

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
        _binding = FragmentReminderListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        layoutManager = LinearLayoutManager(activity)

        val _reminderRecyclerView = view.findViewById<RecyclerView>(R.id.reminderRecyclerView)
        _reminderRecyclerView.layoutManager = layoutManager

        adapter = ReminderRecyclerAdapter(reminder_items)
        _reminderRecyclerView.adapter = adapter

        (adapter as ReminderRecyclerAdapter).setOnItemClickCallback(object: ReminderRecyclerAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Reminder) {
                val mBundle = Bundle()
                mBundle.putParcelable("param1", data)
                view.findNavController().navigate(R.id.action_miHome_to_reminderDetailActivity, mBundle)
            }

            override fun onItemDeleted(id: String) {
                deleteItem(id)
            }
        })

    }

    private suspend fun getUserName(id: String): String {
        var result: String? = null

        val userRef = db.collection("users")
        val userData = userRef.document(id).get().await()
        result = userData.data?.get("full_name").toString()

        return result

    }

    private fun fetchDataFromDb() {
        reminder_items.clear()

        val pDialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#ff2647")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            val docRef = db.collection("schedule")
            var documents = docRef.whereEqualTo("initiator", Functions.loggedInUser!!.uid)
                .get()
                .await()
            val currentDate = SimpleDateFormat("yyyy-M-dd").format(Date())
            val arrCurDate = currentDate.split("-").toTypedArray()
            for (document in documents) {
                val meetingDate = document.get("date").toString().split("-").toTypedArray()
                var image = R.drawable.waiting_meeting
                when {
                    meetingDate[0].toInt() < arrCurDate[0].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                    meetingDate[1].toInt() < arrCurDate[1].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                    meetingDate[2].toInt() < arrCurDate[2].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                }

                reminder_items.add(Reminder(image, document.id, document.data.get("name").toString(), document.data.get("date").toString(), document.data.get("time").toString(),
                    getUserName(document.data.get("initiator").toString()), document.data.get("link").toString(), document.data.get("notes").toString()))
            }

            documents = docRef.whereArrayContains("participants", Functions.loggedInUser.uid)
                .get()
                .await()
            for (document in documents) {
                val meetingDate = document.get("date").toString().split("-").toTypedArray()
                var image = R.drawable.waiting_meeting
                when {
                    meetingDate[0].toInt() < arrCurDate[0].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                    meetingDate[1].toInt() < arrCurDate[1].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                    meetingDate[2].toInt() < arrCurDate[2].toInt() -> {
                        image = R.drawable.online_meeting
                    }
                }
                reminder_items.add(Reminder(image, document.id, document.data.get("name").toString(), document.data.get("date").toString(), document.data.get("time").toString(),
                    getUserName(document.data.get("initiator").toString()), document.data.get("link").toString(), document.data.get("notes").toString()))
            }


            withContext(Dispatchers.Main) {
                reminder_items.sortByDescending { it.date }
                pDialog.hide()
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun deleteItem(id: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val docRef = db.collection("schedule").document(id)
            val document = docRef.get().await()

            if (document != null) {
                if (document.data?.get("initiator").toString() == Functions.loggedInUser!!.uid) {

                    withContext(Dispatchers.Main) {

                        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("If you delete this, the schedule will delete on your participant's account too.")
                            .setConfirmText("Yes, delete it!")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()

                                docRef.delete()
                                fetchDataFromDb()

                                SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("You have successfully delete this item.")
                                    .show()

                            }
                            .show()

                    }
                } else {

                    withContext(Dispatchers.Main) {

                        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("If you delete this, you will removed from meeting participants list.")
                            .setConfirmText("Yes, delete it!")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()

                                docRef.update("participants", FieldValue.arrayRemove(Functions.loggedInUser.uid))
                                fetchDataFromDb()

                                SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("You clicked the button!")
                                    .show()

                            }
                            .show()
                    }
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        fetchDataFromDb()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReminderListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReminderListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}