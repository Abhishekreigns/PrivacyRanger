package com.reignscanary.privacyranger

import android.content.Context
import android.graphics.*
import android.media.Image
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.reignscanary.privacyranger.backend.FaceAnalyser
import com.reignscanary.privacyranger.backend.FaceNetModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception




val faceLists = ArrayList<Pair<String,FloatArray>>()
fun detectFaces(image: InputImage, applicationContext: Context,name: String,faceNetModel: FaceNetModel)  : ArrayList<Pair<String, FloatArray>> {

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .enableTracking()
        .build()



val faceAnalyser = FaceAnalyser(faceNetModel)
    val detector = FaceDetection.getClient(options)
   println("In Detect Faces")

    detector.process(image)
        .addOnSuccessListener { faces ->
            CoroutineScope( Dispatchers.Default ).launch {

            }

                for (face in faces) {


                    val bitmap = Bitmap.createBitmap(image.width,image.height,Bitmap.Config.ARGB_8888)
                    try{
                        val scaledBitmap= cropRectFromBitmap(bitmap,face.boundingBox)
                        val embedding = faceNetModel.getFaceEmbedding( scaledBitmap)

                        println("ADDED FACE")

                            faceLists.add(Pair(name,embedding))




        //
        //        subject= faceAnalyser.getFaceEmbedding(scaledBitmap)
        //               Toast.makeText(applicationContext,"Face Bounds${face.boundingBox}",Toast.LENGTH_SHORT).show()
        //         println("Face Bounds${face.boundingBox}")
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

                    if(!faceLists.isNullOrEmpty()){
                        println("SENT FACE DATA")

                    }
            }}
      //  }
        .addOnFailureListener { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)
                .show()

        }
    return faceLists
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


