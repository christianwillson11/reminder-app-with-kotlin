package com.example.reminderapp

import android.os.Parcel
import android.os.Parcelable

data class FriendData(
    var profile_picture: Int,
    var id: String?,
    var full_name: String?,
    var username: String?,
    var friendStatus: Boolean

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(profile_picture)
        parcel.writeString(id)
        parcel.writeString(full_name)
        parcel.writeString(username)
        parcel.writeByte(if (friendStatus) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FriendData> {
        override fun createFromParcel(parcel: Parcel): FriendData {
            return FriendData(parcel)
        }

        override fun newArray(size: Int): Array<FriendData?> {
            return arrayOfNulls(size)
        }
    }
}
