package com.example.reminderapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class FriendsListRecyclerAdapter (private val listFriend: ArrayList<FriendData>): RecyclerView.Adapter<FriendsListRecyclerAdapter.ViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: FriendData, mode: Int)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_requests_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = listFriend[position]
        holder.itemImage.setImageResource(friend.profile_picture)
        holder.itemFriendName.text = friend.full_name
        holder.itemFriendUID.text = friend.username

        if (friend.friendStatus) {
            holder.declineBtn.visibility = View.GONE
            holder.acceptBtn.visibility = View.GONE
        }

    }


    override fun getItemCount(): Int {
        return listFriend.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemFriendName: TextView
        var itemFriendUID: TextView
        var acceptBtn: Button
        var declineBtn: Button

        init {
            itemImage = itemView.findViewById(R.id.profilePictureImgView)
            itemFriendName = itemView.findViewById(R.id.tvFriendReqName)
            itemFriendUID = itemView.findViewById(R.id.tvFriendReqUsername)
            acceptBtn = itemView.findViewById(R.id.acceptBtn)
            declineBtn = itemView.findViewById(R.id.declineBtn)

            acceptBtn.setOnClickListener {
                val position: Int = adapterPosition
                onItemClickCallback.onItemClicked(listFriend[position], 1)
            }

            declineBtn.setOnClickListener {
                val position: Int = adapterPosition
                onItemClickCallback.onItemClicked(listFriend[position], 2)
            }

        }

    }
}