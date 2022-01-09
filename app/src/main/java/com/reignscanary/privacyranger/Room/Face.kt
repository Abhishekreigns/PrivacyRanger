package com.reignscanary.privacyranger.Room


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Face(
    @PrimaryKey(autoGenerate = true)
    val faceId : Int =0,
    @ColumnInfo(name="Face_name")
    val NameOfFace : String?
)