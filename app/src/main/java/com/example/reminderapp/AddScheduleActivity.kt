package com.example.reminderapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import notification.NotificationData
import notification.PushNotification
import notification.RetrofitInstance
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private var filesUri = arrayListOf<Uri>()
    private var filesUrlOnFirebaseStorage = arrayListOf<String>()
    private var invitedFriends = arrayListOf<String>()
    private var invitedFriendsToken = arrayListOf<String>()
    private var name = ""
    private var status = 1
    lateinit var docId: String

    lateinit var tvDate: TextView
    lateinit var tvTime: TextView
    lateinit var meetingName: EditText
    lateinit var link:EditText
    lateinit var notes: EditText
    lateinit var tvFriends: TextView

    lateinit var date: String

    companion object {
        const val item_data = "kirimDataItem"
        const val item_data2 = "kirimDataNama"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_schedule)

        GlobalScope.launch(Dispatchers.IO){
            name = db.collection("users").document(Functions.loggedInUser!!.uid).get().await().data?.get("full_name").toString()
        }



        meetingName = findViewById(R.id.editTextMeetingName)
        link = findViewById(R.id.editTextLink)
        notes = findViewById(R.id.editTextNotes)
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvFriends = findViewById(R.id.tvFriends)

        val dataIntent = intent.getParcelableExtra<Reminder>("reminderDetailData")

        if (dataIntent != null) {
            docId = dataIntent.id.toString()
            meetingName.setText(dataIntent.name)
            link.setText(dataIntent.link)
            notes.setText(dataIntent.description)
            tvDate.text = dataIntent.date
            date = dataIntent.date.toString()
            tvTime.text = dataIntent.time
            db.collection("schedule").document(dataIntent.id.toString())
                .get()
                .addOnSuccessListener { document ->
                    var tmp = document.get("participants").toString()
                    if (tmp != "[]") {
                        tmp = Functions.SanitizeString(tmp)
                    }
                    val participants = tmp.split(" ").toTypedArray()
                    for (participant in participants) {
                        invitedFriends.add(participant)

                        db.collection("users").document(participant)
                            .get()
                            .addOnSuccessListener { doc ->
                                if (tvFriends.text == "") {
                                    tvFriends.text = doc.get("full_name").toString()
                                } else {
                                    tvFriends.text = "${tvFriends.text}, ${doc.get("full_name")}"
                                }
                            }
                    }
                }
            status = 2
        }

        val backBtn = findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            val eIntent = Intent(this@AddScheduleActivity, HomeActivity::class.java)
            setResult(Activity.RESULT_CANCELED, eIntent)
            finish()
        }

        val pickerDateBtn = findViewById<Button>(R.id.pickDateBtn)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val h_month = arrayListOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

        pickerDateBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                tvDate.setText("" + dayOfMonth + " " + h_month[monthOfYear] + " " + year)
                if (dayOfMonth < 10) {
                    date = "${year}-${monthOfYear+1}-0${dayOfMonth}"
                } else {
                    date = "${year}-${monthOfYear+1}-${dayOfMonth}"
                }

            }, year, month, day)
            dpd.show()
        }

        val pickTimeBtn = findViewById<Button>(R.id.pickTimeBtn)
        pickTimeBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                tvTime.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        val chooseFriendsBtn = findViewById<Button>(R.id.chooseFriendsBtn)
        chooseFriendsBtn.setOnClickListener {
            val eIntent = Intent(this@AddScheduleActivity, ChooseFriendsActivity::class.java)
            eIntent.apply {
                putExtra("selection", invitedFriends)
            }
            startActivityForResult(eIntent, 12)
        }


        val uploadImgBtn = findViewById<Button>(R.id.uploadImageBtn)
        uploadImgBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            startActivityForResult(intent, 100)
        }

        val submitBtn = findViewById<ImageButton>(R.id.submitBtn)

        submitBtn.setOnClickListener {

            if (!isDataValid()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Please fill all data completely")
                    .setConfirmText("OK")
                    .show()
                return@setOnClickListener
            } else {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Uploading files and data...")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }

            val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)

            if (status == 1) {
                if (filesUri.isEmpty()) {
                    uploadDataToFirestore()
                } else {
                    for (uri in filesUri) {
                        uploadFile(uri, fileName)
                    }
                }
            } else {
                if (filesUri.isEmpty()) {
                    updateDataToFirestore(dataIntent?.id.toString())
                } else {
                    for (uri in filesUri) {
                        uploadFile(uri, fileName)
                    }
                }
            }

        }


    }

    private fun isDataValid(): Boolean {
        if (meetingName.text.toString() == "" || invitedFriends.isEmpty() ||
            tvDate.text.toString() == "" || tvTime.text.toString() == "" || link.text.toString() == "" ||
            notes.text.toString() == "") {
            return false
        }
        return true
    }

    private fun uploadDataToFirestore() {
        val loggedInUser = FirebaseAuth.getInstance().getCurrentUser()

        val schedule = hashMapOf(
            "initiator" to loggedInUser!!.uid,
            "name" to meetingName.text.toString(),
            "date" to date,
            "notes" to notes.text.toString(),
            "time" to tvTime.text.toString(),
            "participants" to invitedFriends,
            "link" to link.text.toString(),
            "files" to filesUrlOnFirebaseStorage
        )

        db.collection("schedule")
            .add(schedule)
            .addOnSuccessListener {

                //TODO = LOOP FOR ALL USERS

                for (token in invitedFriendsToken) {
                    PushNotification(
                        NotificationData("New Meeting from $name", "${meetingName.text} at ${tvDate.text} - ${tvTime.text}"),
                        token
                    ).also {
                        sendNotification(it)
                    }
                }

                val eIntent = Intent(this@AddScheduleActivity, HomeActivity::class.java)
                setResult(Activity.RESULT_OK, eIntent)
                finish()
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Fail to Upload Data")
                    .setContentText("Please try again.\nError: $exception")
                    .setConfirmText("OK")
                    .show()
            }


    }

    private fun uploadFile(fileUri: Uri, folderName: String) {
        val fileName = File(fileUri.path).name
        val storageReference = Firebase.storage.reference.child("${folderName}/${fileName}")

        storageReference.putFile(fileUri).continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let {
                throw it
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->

            if (task.isSuccessful && status == 1) {
                val downloadUri = task.result
                filesUrlOnFirebaseStorage.add(downloadUri.toString())
                Toast.makeText(this, "FILE SUCCESSFULLY UPLOADED", Toast.LENGTH_LONG).show()

                if (filesUri.size == filesUrlOnFirebaseStorage.size) {
                    uploadDataToFirestore()
                }

            } else if (task.isSuccessful && status == 2) {
                val downloadUri = task.result
                filesUrlOnFirebaseStorage.add(downloadUri.toString())
                Toast.makeText(this, "FILE SUCCESSFULLY UPLOADED", Toast.LENGTH_LONG).show()

                if (filesUri.size == filesUrlOnFirebaseStorage.size) {
                    updateDataToFirestore(docId, true)
                }
            } else {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Cannot Upload!")
                    .setContentText("An error occurred")
                    .setConfirmText("OK")
                    .show()
            }
        }
        Thread.sleep(5_000)

    }

    private fun updateDataToFirestore(id: String, isFileUpdated: Boolean = false) {
        if (!isFileUpdated) {
            db.collection("schedule").document(id)
                .update("name", meetingName.text.toString(),
                    "date", date,
                    "notes", notes.text.toString(),
                    "time", tvTime.text.toString(),
                    "participants", invitedFriends,
                    "link", link.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this, "Data successfully updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
        } else {
            db.collection("schedule").document(id)
                .update("name", meetingName.text.toString(),
                    "date", date,
                    "notes", notes.text.toString(),
                    "time", tvTime.text.toString(),
                    "participants", invitedFriends,
                    "link", link.text.toString(),
                    "files", filesUrlOnFirebaseStorage)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data successfully updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)

            if (response.isSuccessful) {
                Log.d("FirebaseMsg", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("FirebaseMsg", response.errorBody().toString())
            }

        } catch (e: Exception) {
            Log.e("FirebaseMsg", e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) { 100 ->
            if (resultCode == Activity.RESULT_OK) {
                if (null != data) {
                    if (null !=data.clipData) {
                        for (i in 0 until data.clipData!!.itemCount) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            filesUri.add(uri)
                        }
                    } else {
                        val uri = data.data
                        filesUri.add(uri!!)
                    }
                }
            }
        }

        if (requestCode == 12) {
            if (data != null) {
                if (resultCode == Activity.RESULT_OK) {
                    val intentItemFriendsId = data.getStringArrayListExtra(item_data)
                    invitedFriends = intentItemFriendsId!!

                    val intentItemFriendsName = data.getStringArrayListExtra(item_data2)

                    tvFriends.text = intentItemFriendsName.toString()

                    GlobalScope.launch(Dispatchers.IO) {
                        for (invitedFriendId in invitedFriends) {
                            val document = db.collection("users").document(invitedFriendId).get().await()
                            invitedFriendsToken.add(document.data?.get("token").toString())
                        }
                    }
                }
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext, "You canceled the action", Toast.LENGTH_LONG).show()
            }
        }


    }
}