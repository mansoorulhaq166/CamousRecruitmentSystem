package com.example.campusrecruitmentsystem.models.main

import android.os.Parcel
import android.os.Parcelable

data class Job(
    val id:String = "",
    val title: String = "",
    val criteria: String = "",
    val company: String = "",
    val location: String = "",
    val description: String = "",
    val salary: String = "",
    val deadline: String = ""
) : Parcelable {
    // Your existing constructor and other methods...

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(title)
        dest.writeString(criteria)
        dest.writeString(company)
        dest.writeString(location)
        dest.writeString(description)
        dest.writeString(salary)
        dest.writeString(deadline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Job> {
        override fun createFromParcel(parcel: Parcel): Job {
            return Job(parcel)
        }

        override fun newArray(size: Int): Array<Job?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )
}