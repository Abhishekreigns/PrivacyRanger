package com.reignscanary.privacyranger.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Face::class], version = 1)
abstract class FaceDatabase : RoomDatabase(){

abstract fun faceDao() : FaceDao
    companion object{
        var instance : FaceDatabase? = null
        @Synchronized
        fun getInstance(context : Context): FaceDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    FaceDatabase::class.java,
                    "Face Database"
                )
                    .build()
            }
            return instance!!
        }


        }

}



