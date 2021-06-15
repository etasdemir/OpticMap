package com.elacqua.opticmap.data.local

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Place(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: String = "",
    val imageDir: String = "",
): Parcelable {
    @Ignore
    var address: Address = Address()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Place
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = (31 * result + latitude).toInt()
        result = (31 * result + longitude).toInt()
        result = 31 * result + date.hashCode()
        result = 31 * result + imageDir.hashCode()
        return result
    }
}