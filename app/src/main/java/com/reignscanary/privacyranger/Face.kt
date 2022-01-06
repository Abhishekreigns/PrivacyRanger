package com.reignscanary.privacyranger


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Face(
    @PrimaryKey(autoGenerate = true)
    val faceId : Int =0,



)