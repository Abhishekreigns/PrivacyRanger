package com.reignscanary.privacyranger

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.runtime.MutableState
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

 fun saveImage(image: Bitmap,applicationContext : Context,userName : MutableState<String>) {
    println("SAVING")
    val directory  : File? = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)


    if(!directory?.exists()!!)
    { println("NO EXIST")
        val userImageDirectory = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        userImageDirectory?.mkdir()

    }

    val file = File(directory ,"${userName.value}_${System.currentTimeMillis()}.jpg")


    try{
        val imageFile = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG,100,imageFile)


        imageFile.flush()
        imageFile.close()
        println("SAVING FILE")

    }
    catch (e: Exception){
        println("ERROR SAVING FILE ${e.localizedMessage}")
    }


}