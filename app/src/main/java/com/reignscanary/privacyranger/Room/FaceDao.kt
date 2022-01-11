package com.reignscanary.privacyranger.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FaceDao {

    @Query("Select * from Face")
    suspend fun  faceList():List<Face>
    @Insert
    suspend fun addFace(face: Face)

}