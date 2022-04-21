package com.example.reminderapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ReminderRecyclerAdapter (private val listReminder: ArrayList<Reminder>): RecyclerView.Adapter<ReminderRecyclerAdapter.ViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: Reminder)
        fun onItemDeleted(id: String)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reminder_card_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = listReminder[position]
        holder.itemImage.setImageResource(reminder.image)
        holder.itemTitle.text = reminder.name
        val properDate = reminder.date!!.split("-").toTypedArray()
        val month = arrayListOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        holder.itemDate.text = "${properDate[2]} ${month[properDate[1].toInt()-1]} ${properDate[0]}\n${reminder.time}"
        holder.itemInitiator.text = reminder.initiator
    }

    override fun getItemCount(): Int {
        return listReminder.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDate: TextView
        var itemInitiator: TextView
        var deleteBtn: ImageView

        init {
            itemImage = itemView.findViewById(R.id.reminder_image)
            itemTitle = itemView.findViewById(R.id.tvTitle)
            itemDate = itemView.findViewById(R.id.tvDate)
            itemInitiator = itemView.findViewById(R.id.tvInitiator)
            deleteBtn = itemView.findViewById(R.id.deleteBtn)

            itemView.setOnClickListener {
                val position: Int = adapterPosition
                onItemClickCallback.onItemClicked(listReminder[position])
            }

            deleteBtn.setOnClickListener {
                val position: Int = adapterPosition
                onItemClickCallback.onItemDeleted(listReminder[position].id.toString())
            }

        }
    }

}