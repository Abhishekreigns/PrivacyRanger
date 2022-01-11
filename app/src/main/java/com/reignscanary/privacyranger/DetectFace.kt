package com.reignscanary.privacyranger

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.lang.Exception

var faceId = mutableStateOf(0)
 var faceShapeBounds = mutableStateOf(Rect())

 var faceShapeBoundsLeft = mutableStateOf(0)

 var faceShapeBoundsTop = mutableStateOf(0)

 var faceShapeBoundsRight = mutableStateOf(0)

 var faceShapeBoundsBottom = mutableStateOf(0)



fun detectFaces(image: InputImage, applicationContext: Context) {

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()
     var subject = FloatArray(192)

val faceAnalyser = FaceAnalyser(applicationContext)
    val detector = FaceDetection.getClient(options)
   println("In Detect Faces")

    detector.process(image)
        .addOnSuccessListener { faces ->
            CoroutineScope( Dispatchers.Default ).launch {

            }
          //  else {
                for (face in faces) {
                    val bitmap = Bitmap.createBitmap(image.width,image.height,Bitmap.Config.ARGB_8888)
                    try{
                    val scaledBitmap= cropRectFromBitmap(bitmap,face.boundingBox)
                   subject= faceAnalyser.getFaceEmbedding(scaledBitmap)
                          Toast.makeText(applicationContext,"Face Bounds${face.boundingBox}",Toast.LENGTH_SHORT).show()
                    println("Face Bounds${face.boundingBox}")
           //      faceShapeBounds.value = face.boundingBox
           //
           //
               //FfaceShapeBoundsLeft.value= face.boundingBox.left
             //  faceShapeBoundsTop.value= face.boundingBox.top
               //faceShapeBoundsRight.value=  face.boundingBox.right
           //    faceShapeBoundsBottom.value=face.boundingBox.bottom

                }
                    catch (e: Exception){
                        println("ERROR DETECTING $e.localizedMessage")
                    }
            }}
      //  }
        .addOnFailureListener { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)
                .show()

        }
}
// Crop the given bitmap with the given rect.
fun cropRectFromBitmap(source: Bitmap, rect: Rect ): Bitmap {
    var width = rect.width()
    var height = rect.height()
    if ( (rect.left + width) > source.width ){
        width = source.width - rect.left
    }
    if ( (rect.top + height ) > source.height ){
        height = source.height - rect.top
    }
    val croppedBitmap = Bitmap.createBitmap( source , rect.left , rect.top , width , height )
    // Uncomment the below line if you want to save the input image.
    // BitmapUtils.saveBitmap( context , croppedBitmap , "source" )
    return croppedBitmap
}

fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val yuv = out.toByteArray()
    var output = BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
    output = rotateBitmap( output , rotationDegrees.toFloat() )
    return flipBitmap( output )

}
fun rotateBitmap(source: Bitmap, degrees : Float ): Bitmap {
    val matrix = Matrix()
    matrix.postRotate( degrees )
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix , false )
}

fun flipBitmap( source: Bitmap ): Bitmap {
    val matrix = Matrix()
    matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}


