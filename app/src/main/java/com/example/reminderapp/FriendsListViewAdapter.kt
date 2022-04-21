package com.example.reminderapp

import android.widget.ArrayAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class FriendsListViewAdapter(private val listFriend: ArrayList<FriendData>, private val selection: ArrayList<String>): RecyclerView.Adapter<FriendsListViewAdapter.ViewHolder>() {

    private val friendId = arrayListOf<String>()
    private val friendName = arrayListOf<String>()

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: List<String>, data2: List<String>)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = listFriend[position]
        holder.itemName.text = friend.full_name

        if (selection.contains(friend.id)) {
            holder.checkbox.isChecked = true
        }

        holder.checkbox.setOnClickListener {
            if (holder.checkbox.isChecked) {
                friendId.add(friend.id.toString())
                friendName.add(friend.full_name.toString())
            } else {
                if (friendId.contains(friend.id.toString())) {
                    friendId.remove(friend.id.toString())
                    friendName.remove(friend.full_name.toString())
                }
            }
            onItemClickCallback.onItemClicked(friendId, friendName)
        }


    }

    override fun getItemCount(): Int {
        return listFriend.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemName: TextView
        var checkbox: CheckBox
        init {
            itemName = itemView.findViewById(R.id.tvName)
            checkbox = itemView.findViewById(R.id.checkBox)
        }
    }

}