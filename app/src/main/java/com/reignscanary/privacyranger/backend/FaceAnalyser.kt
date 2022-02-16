package com.reignscanary.privacyranger.backend

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.reignscanary.privacyranger.*
import com.reignscanary.privacyranger.activities.predictedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt


@SuppressLint("UnsafeOptInUsageError")
class FaceAnalyser( private var model : FaceNetModel) : ImageAnalysis.Analyzer {

    private val nameScoreHashmap = HashMap<String,ArrayList<Float>>()

private var currentFace = FloatArray(model.modelInfo.outputDims)
    var faceList = faceLists
    override fun analyze(imageProxy: ImageProxy) {


        //Converting the image from live frame to bitmap
        val bitmap = imageToBitmap(imageProxy.image!!,imageProxy.imageInfo.rotationDegrees)
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!,imageProxy.imageInfo.rotationDegrees)
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode( FaceDetectorOptions.PERFORMANCE_MODE_FAST )
            .build()
        val detector = FaceDetection.getClient(realTimeOpts)


           //Processing the input imae from live frame of the camera
        detector.process(inputImage)
            .addOnSuccessListener {

                  //Live frame faces
                faces->
               CoroutineScope(Dispatchers.Default).launch{
                    //Running the ML model on the faces and passing the bitmap of the frame to crop it
                   runModel(faces,bitmap,)

               }


            }
            .addOnCompleteListener{
                //After each frame captured,closing the image so  as to capture the next frame or new face that appears
                //on the camera preview
                imageProxy.image!!.close()
                imageProxy.close()            }


    }

    private suspend fun runModel(faces: List<Face>, bitmap: Bitmap) {
        val predictions = ArrayList<Prediction>()
      withContext(Dispatchers.Default){
    for(face in faces){

        try{
             //Cropping the bitmap of the face in the frame to process only the face boundary
            val scaledBitmap= cropRectFromBitmap(bitmap,face.boundingBox)


            //Getting the face Embedding of that bitmap as a float array(have to store and process later)
            for ( i in 0 until faceList.size ) {
                println("SCANNING FACELIST: ${faceList[i].first}")
                // If this cluster ( i.e an ArrayList with a specific key ) does not exist,
                // initialize a new one.
                currentFace= model.getFaceEmbedding(scaledBitmap)
                if ( nameScoreHashmap[ faceList[ i ].first ] == null ) {
                    // Compute the L2 norm and then append it to the ArrayList.
                    val p = ArrayList<Float>()

                    p.add( L2Norm( currentFace , faceList[ i ].second ) )

                    nameScoreHashmap[ faceList[ i ].first ] = p
                }
                // If this cluster exists, append the L2 norm/cosine score to it.
                else {

                    nameScoreHashmap[ faceList[ i ].first ]?.add( L2Norm( currentFace , faceList[ i ].second ) )
                }
            }

            val avgScores = nameScoreHashmap.values.map{ scores -> scores.toFloatArray().average() }
            val names = nameScoreHashmap.keys.toTypedArray()
            nameScoreHashmap.clear()
            println("DETECTED FACE: ${avgScores.minOrNull()!!}")
            val bestScoreUserName: String =
                // In case of cosine similarity, choose the highest value.
                if ( avgScores.minOrNull()!! > model.modelInfo.l2Threshold ) {

                    "Unknown"

                }
                else {
                    names[ avgScores.indexOf( avgScores.minOrNull()!! ) ]

                }

            predictions.add(
                Prediction(
                    face.boundingBox,
                    bestScoreUserName
                )
            )
             predictedName.value = bestScoreUserName
             println("DETECTED FACE: $bestScoreUserName")

        }
        catch (e:Exception){
            println("ERROR SCANNING $e.localizedMessage")
        }
    }
}
    }

    // Compute the L2 norm of ( x2 - x1 )
    private fun L2Norm( x1 : FloatArray, x2 : FloatArray ) : Float {
        return sqrt( x1.mapIndexed{ i , xi -> (xi - x2[ i ]).pow( 2 ) }.sum() )
    }


    private fun cosineSimilarity( x1 : FloatArray , x2 : FloatArray ) : Float {
        val mag1 = sqrt( x1.map { it * it }.sum() )
        val mag2 = sqrt( x2.map { it * it }.sum() )
        val dot = x1.mapIndexed{ i , xi -> xi * x2[ i ] }.sum()
        return dot / (mag1 * mag2)
    }
















}


