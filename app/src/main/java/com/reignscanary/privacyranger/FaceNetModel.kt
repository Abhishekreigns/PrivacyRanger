package com.reignscanary.privacyranger

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class FaceNetModel(context : Context,
                    var modelInfo : ModelInfo,
                    useGpu : Boolean,
                    useXNNPack : Boolean) {


    // Input image size for FaceNet model.
    private val imgSize = modelInfo.inputDims

    // Output embedding size
    val embeddingDim = modelInfo.outputDims
    //InterPreter to interpret the tflite model given(Read about tensorflow in general)
    private lateinit var interpreter : Interpreter
    //Use this to convert incoming bitmap to buffer (will be used on the scaled bitmap,now skip it,you will understand at the end of the code)
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add( ResizeOp(imgSize , imgSize , ResizeOp.ResizeMethod.BILINEAR ) )
        .add( StandardizeOp() )
        .build()

    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            setUseXNNPACK( useXNNPack )
        }
        interpreter = Interpreter(FileUtil.loadMappedFile(context, modelInfo.assetsFilename ) , interpreterOptions )

    }

    fun getFaceEmbedding(image : Bitmap) : FloatArray {
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
        val faceNetModelOutputs = Array( 1 ){ FloatArray(embeddingDim) }
        interpreter.run( inputs, faceNetModelOutputs )
        println("MYMESSAGE: FACE" + faceNetModelOutputs[0])
        return faceNetModelOutputs
    }



    //Helper class to hlp image processor to standardize output(will have to look at it a bit)
    class StandardizeOp : TensorOperator {
        override fun apply(p0: TensorBuffer?): TensorBuffer {
            val pixels = p0!!.floatArray
            val mean = pixels.average().toFloat()
            var std = sqrt(pixels.map { pi -> (pi - mean).pow(2) }.sum() / pixels.size.toFloat())
            std = max(std, 1f / sqrt(pixels.size.toFloat()))
            for (i in pixels.indices) {
                pixels[i] = (pixels[i] - mean) / std
            }
            val output = TensorBufferFloat.createFixedSize(p0.shape, DataType.FLOAT32)
            output.loadArray(pixels)
            return output
        }


    }

}