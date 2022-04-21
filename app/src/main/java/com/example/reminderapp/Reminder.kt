package com.example.reminderapp

import android.os.Parcel
import android.os.Parcelable

data class Reminder(
    var image: Int,
    var id: String?,
    var name: String?,
    var date: String?,
    var time: String?,
    var initiator: String?,
    var link: String?,
    var description: String?,
    var isAlarmSet: Boolean? = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(image)
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(initiator)
        parcel.writeString(link)
        parcel.writeString(description)
        parcel.writeValue(isAlarmSet)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reminder> {
        override fun createFromParcel(parcel: Parcel): Reminder {
            return Reminder(parcel)
        }

        override fun newArray(size: Int): Array<Reminder?> {
            return arrayOfNulls(size)
        }
    }
}