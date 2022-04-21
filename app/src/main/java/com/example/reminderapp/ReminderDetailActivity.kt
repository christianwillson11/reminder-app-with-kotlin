package com.example.reminderapp

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ReminderDetailActivity : AppCompatActivity() {
    @SuppressLint("CutPasteId")
    val db = Firebase.firestore
    lateinit var sp: SharedPreferences

    lateinit var tvParticipant: TextView
    lateinit var filesContainer: LinearLayout
    lateinit var floatingBtnRemindMe: FloatingActionButton
    lateinit var btnDownload: Button
    lateinit var btnEdit: Button


    private var myDownloadId: Long = 0
    private var scheduleId: String? = null
    private var meetingFilesUrlString = arrayListOf<String>()

    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private var alarmedSchedule = arrayListOf<String>()
    private lateinit var id: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_detail)

        val dataIntent = intent.getParcelableExtra<Reminder>("param1")

        floatingBtnRemindMe = findViewById(R.id.floatingBtnRemindMe)

        floatingBtnRemindMe.setOnClickListener {
            setAlarm()
        }

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        val tvInitiator = findViewById<TextView>(R.id.tvInitiator)

        val tvMeetingLink = findViewById<TextView>(R.id.tvMeetingLink)
        val tvDateTime = findViewById<TextView>(R.id.tvDateTime)

        tvParticipant = findViewById(R.id.tvParticipant)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)

        filesContainer = findViewById(R.id.fileLinearLayoutContainer)

        if (dataIntent != null) {
            fetchSpData()
            id = dataIntent.id.toString()

            if (alarmedSchedule.contains(id)) {
                floatingBtnRemindMe.setImageResource(R.drawable.check)
                floatingBtnRemindMe.isEnabled = false
            }

            collapsingToolbar.title = dataIntent.name
            tvInitiator.text = dataIntent.initiator
            tvMeetingLink.text = dataIntent.link
            val properDate = dataIntent.date!!.split("-").toTypedArray()
            val month = arrayListOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            tvDateTime.text = "${properDate[2]} ${month[properDate[1].toInt()-1]} ${properDate[0]} / ${dataIntent.time}"
            tvDescription.text = dataIntent.description

            GlobalScope.launch(Dispatchers.IO) {
                fetchFiles(dataIntent.id.toString())
            }

            btnEdit = findViewById(R.id.btnEdit)
            db.collection("schedule").document(dataIntent.id.toString()).get()
                .addOnSuccessListener { document ->
                    if (document.get("initiator").toString() != Functions.loggedInUser!!.uid) {
                        btnEdit.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    btnEdit.visibility = View.GONE
                }


            setDateTimeToInt(dataIntent.date.toString(), dataIntent.time.toString())
            scheduleId = dataIntent.id
            createNotificationChannel(dataIntent.name.toString(), "${dataIntent.date} / ${dataIntent.time}")

            tvParticipant.text = ""

            db.collection("schedule").document(dataIntent.id.toString()).get()
                .addOnSuccessListener { document ->
                    var tmp = document.get("participants").toString()
                    if (tmp != "[]") {
                        tmp = Functions.SanitizeString(tmp)
                    }
                    val friends = tmp.split(" ").toTypedArray()
                    for (id in friends) {
                        fetchFriendsData(id)
                    }
                }

            //z

            btnDownload = findViewById(R.id.btnDownload)

            btnDownload.setOnClickListener {
                for (filePath in meetingFilesUrlString) {
                    val request = DownloadManager.Request (
                        Uri.parse(filePath))
                        .setTitle(filePath)
                        .setDescription("Downloading Files...")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

                        .setAllowedOverMetered(true)

                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    myDownloadId = dm.enqueue(request)
                }
            }

            val br = object: BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == myDownloadId) {
                        Toast.makeText(applicationContext, "All Files Has Been Successfully Downloaded", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            btnEdit.setOnClickListener {
                val intent = Intent(this, AddScheduleActivity::class.java).apply {
                    putExtra("reminderDetailData", dataIntent)
                }
                startActivity(intent)
            }
        }

    }

    private suspend fun fetchFiles(id: String) {

        var tmp = db.collection("schedule").document(id).get().await().get("files").toString()
        withContext(Dispatchers.Main) {
            if (tmp != "[]") {
                tmp = Functions.SanitizeString(tmp)
                val files = tmp.split(" ").toTypedArray()
                for (filePath in files) {
                    meetingFilesUrlString.add(filePath)

                    val tvFile = TextView(this@ReminderDetailActivity)
                    tvFile.textSize = 16f
                    val filename = filePath.substring(filePath.lastIndexOf("/")+1)
                    tvFile.text = filename
                    filesContainer.addView(tvFile)

                }
            } else {
                findViewById<TextView>(R.id.textView8).visibility = View.GONE
                btnDownload.visibility = View.GONE
            }
        }

    }

    private fun fetchFriendsData(id: String) {
        db.collection("users").document(id).get()
            .addOnSuccessListener { document ->
                if (document.get("full_name").toString() != "null") {
                    var string = tvParticipant.text.toString()
                    string += "${document.get("full_name").toString()}\n"
                    tvParticipant.text = string
                }
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error!")
                    .setContentText("Error: $exception")
                    .setConfirmText("OK")
                    .show()
            }
    }

    private fun setDateTimeToInt(date: String, time: String) {
        val arrDate = date.split("-").toTypedArray()

        val day_of_month = arrDate[2].toInt()
        val month = arrDate[1].toInt()
        val year = arrDate[0].toInt()

        val arrTime = time.split(":").toTypedArray()

        val hour_of_day = arrTime[0].toInt()
        val minute = arrTime[1].toInt()

        calendar = Calendar.getInstance()
        calendar[Calendar.DAY_OF_MONTH] = day_of_month
        calendar[Calendar.MONTH] = month
        calendar[Calendar.YEAR] = year
        calendar[Calendar.HOUR_OF_DAY] = hour_of_day
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

    }

    private fun createNotificationChannel(meetingName: String, meetingDateTime: String) {
        val name: CharSequence = "scheduleReminderChannel"
        val description = "Channel For Alarm Manager"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("schedule", name, importance)
        channel.description = description

        AlarmReceiver.contentTitle = "You have meeting named: ${meetingName}"
        AlarmReceiver.contentText = "Meeting at ${meetingDateTime}"

        val notificationManager = getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun setAlarm() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,pendingIntent
        )

        alarmedSchedule.add(id)
        saveDataToSp()
        floatingBtnRemindMe.setImageResource(R.drawable.check)
        floatingBtnRemindMe.isEnabled = false

        Toast.makeText(this, "Alarm set Successfully", Toast.LENGTH_SHORT).show()


    }

    private fun fetchSpData() {
        sp = getSharedPreferences("dataSP", Context.MODE_PRIVATE)

        val gson = Gson()
        val isisp = sp.getString("alarmedSchedule", null)

        if (isisp != null) {
            val type = object: TypeToken<ArrayList<String>>() {}.type
            alarmedSchedule = gson.fromJson(isisp, type)
        }
    }

    private fun saveDataToSp() {
        val editor = sp.edit()
        val gson = Gson()
        val json = gson.toJson(alarmedSchedule)
        editor.putString("alarmedSchedule", json)
        editor.apply()
    }

}