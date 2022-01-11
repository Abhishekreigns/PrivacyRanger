package com.reignscanary.privacyranger

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.lang.Exception
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import android.graphics.Bitmap
import android.widget.Toast
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage



@SuppressLint("UnsafeOptInUsageError")
class FaceAnalyser(private var context : Context) : ImageAnalysis.Analyzer {
    //InterPreter to interpret the tflite model given(Read about tensorflow in general)
    private  var interpreter : Interpreter
    //Use this to convert incoming bitmap to buffer (will be used on the scaled bitmap,now skip it,you will understand at the end of the code)
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add( ResizeOp( 112 , 112 , ResizeOp.ResizeMethod.BILINEAR ) )
        .add( StandardizeOp() )
        .build()
    private val nameScoreHashmap = HashMap<String,ArrayList<Float>>()

private var currentFace = FloatArray(192)
    var faceList = ArrayList<Pair<String,FloatArray>>()
    override fun analyze(imageProxy: ImageProxy) {

        //Converting the image from live frame to bitmap
        val bitmap = imageToBitmap(imageProxy?.image!!,imageProxy.imageInfo.rotationDegrees)
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
                   runModel(faces,bitmap)

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
            currentFace= getFaceEmbedding(scaledBitmap)
            //Getting the face Embedding of that bitmap as a float array(have to store and process later)
            for ( i in 0 until faceList.size ) {
                // If this cluster ( i.e an ArrayList with a specific key ) does not exist,
                // initialize a new one.

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
            val bestScoreUserName: String =  // In case of L2 norm, choose the lowest value.
                if ( avgScores.minOrNull()!! > 2f ) {
                    println("DETECTED FACE: UNKNOWN")
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
            println("ERROR $e.localizedMessage")
        }
    }
}
    }

    // Compute the L2 norm of ( x2 - x1 )
    private fun L2Norm( x1 : FloatArray, x2 : FloatArray ) : Float {
        return sqrt( x1.mapIndexed{ i , xi -> (xi - x2[ i ]).pow( 2 ) }.sum() )
    }

     fun getFaceEmbedding(image : Bitmap ) : FloatArray {
        println("MYMESSAGE: RUNNING")
        //Running the facenet model on the scaled bitmap (after converting it to buffer) as model accepts ByteBuffer
        return runFaceNet( convertBitmapToBuffer( image ))[0]
    }

    // Resize the given bitmap and convert it to a ByteBuffer
    private fun convertBitmapToBuffer( image : Bitmap) : ByteBuffer {
        println("MYMESSAGE:converting")
        //The declaration is done at the top for this processor.
        return imageTensorProcessor.process( TensorImage.fromBitmap( image ) ).buffer
    }


    private fun runFaceNet(inputs: Any): Array<FloatArray> {
        //After the conversion to buffer it is fed to the model interpreter to generate outputs from the input buffer we receive
        val faceNetModelOutputs = Array( 1 ){ FloatArray( 192) }
        interpreter.run( inputs, faceNetModelOutputs )
        println("MYMESSAGE: FACE" + faceNetModelOutputs[0])
        return faceNetModelOutputs
    }


    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            setNumThreads(4)
            setUseXNNPACK( true )
        }
        interpreter = Interpreter(FileUtil.loadMappedFile(context, "mobile_face_net.tflite") , interpreterOptions )

    }






}
      //Helper class to hlp image processor to standardize output(will have to look at it a bit)
        class StandardizeOp : TensorOperator {
    override fun apply(p0: TensorBuffer?): TensorBuffer {
        val pixels = p0!!.floatArray
        val mean = pixels.average().toFloat()
        var std = sqrt( pixels.map{ pi -> ( pi - mean ).pow( 2 ) }.sum() / pixels.size.toFloat() )
        std = max( std , 1f / sqrt( pixels.size.toFloat() ))
        for ( i in pixels.indices ) {
            pixels[ i ] = ( pixels[ i ] - mean ) / std
        }
        val output = TensorBufferFloat.createFixedSize( p0.shape , DataType.FLOAT32 )
        output.loadArray( pixels )
        return output
    }

}

