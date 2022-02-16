package com.reignscanary.privacyranger

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import com.google.mlkit.vision.common.InputImage
import com.reignscanary.privacyranger.activities.predictedName
import com.reignscanary.privacyranger.backend.FaceNetModel
import java.io.FileDescriptor

@Composable
fun Register(takeImg : ManagedActivityResultLauncher<Void?,
        Bitmap?>, userName : MutableState<String>,
             applicationContext: Context
             , faceNetModel: FaceNetModel,
             contentResolver: ContentResolver) {
    Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally ) {

        TextButton(onClick = {  takeImg.launch() }) {
            Text("Register  this face ")
        }

        TextButton(onClick = { detectFacesInFile(applicationContext,faceNetModel,contentResolver)}
        ) {
            Text("Detect Details ")

        }
        TextField(value =userName.value ,
            onValueChange = {
                userName.value=it
            },
            label= {
                Text(text = "Enter name to recognize")
            }
        )
        Text(text = predictedName.value)

    }


}


private fun detectFacesInFile(applicationContext : Context, faceNetModel : FaceNetModel, contentResolver: ContentResolver) {


    val userImageFileList =
        applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.listFiles()



    if (userImageFileList != null && userImageFileList.isNotEmpty()) {

        Toast.makeText(
            applicationContext,
            "DETECTION >>> ${userImageFileList.size} images found",
            Toast.LENGTH_SHORT
        ).show()

        for (userImageFile in userImageFileList) {
            println("DETECTED FILE: ${userImageFile.name}")
            val imgFileUri = Uri.fromFile(userImageFile)
            val imageBitmap = getBitmapFromUri(contentResolver, imgFileUri)

            val image = InputImage.fromByteArray(
                bitmapToNV21ByteArray(imageBitmap),
                imageBitmap.width,
                imageBitmap.height,
                0,
                InputImage.IMAGE_FORMAT_NV21
            )


            detectFaces(
                image,
                applicationContext,
                userImageFile.name.dropLast(18),
                faceNetModel
            )


        }
    } else {
        Toast.makeText(
            applicationContext,
            "Register your face atleast once",
            Toast.LENGTH_SHORT
        ).show()
        println("DETECTION  NULL")
    }

}
// Get the image as a Bitmap from given Uri
// Source -> https://developer.android.com/training/data-storage/shared/documents-files#bitmap
fun getBitmapFromUri(contentResolver : ContentResolver, uri: Uri): Bitmap {
    val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
    val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor.close()
    return image
}

// Convert the given Bitmap to NV21 ByteArray
// See this comment -> https://github.com/firebase/quickstart-android/issues/932#issuecomment-531204396
fun bitmapToNV21ByteArray(bitmap: Bitmap): ByteArray {
    val argb = IntArray(bitmap.width * bitmap.height )
    bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val yuv = ByteArray(bitmap.height * bitmap.width + 2 * Math.ceil(bitmap.height / 2.0).toInt()
            * Math.ceil(bitmap.width / 2.0).toInt())
    encodeYUV420SP( yuv, argb, bitmap.width, bitmap.height)
    return yuv
}

private fun encodeYUV420SP(byteArray: ByteArray, argb: IntArray, width: Int, height: Int) {
    val frameSize = width * height
    var yIndex = 0
    var uvIndex = frameSize
    var R: Int
    var G: Int
    var B: Int
    var Y: Int
    var U: Int
    var V: Int
    var index = 0
    for (j in 0 until height) {
        for (i in 0 until width) {
            R = argb[index] and 0xff0000 shr 16
            G = argb[index] and 0xff00 shr 8
            B = argb[index] and 0xff shr 0
            Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
            U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
            V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
            byteArray[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
            if (j % 2 == 0 && index % 2 == 0) {
                byteArray[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                byteArray[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
            }
            index++
        }
    }
}


