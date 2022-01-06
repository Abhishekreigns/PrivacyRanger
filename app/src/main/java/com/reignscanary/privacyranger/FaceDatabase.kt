package com.reignscanary.privacyranger

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Face::class], version = 1)
abstract class FaceDatabase : RoomDatabase(){
}