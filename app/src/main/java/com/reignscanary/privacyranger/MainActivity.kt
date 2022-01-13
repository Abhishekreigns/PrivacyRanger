package com.reignscanary.privacyranger

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.lang.Exception
import java.util.concurrent.Executors

var paintResource  = R.drawable.face
 var predictedName = mutableStateOf("Predicted Name will appear here")
var selectedCamera = mutableStateOf("Front Camera")
class MainActivity : ComponentActivity() {

    lateinit var takeImg : ManagedActivityResultLauncher<Void?, Bitmap?>
    private  var userName : MutableState<String> = mutableStateOf("")
    private lateinit var faceAnalyser: FaceAnalyser
    lateinit var faceNetModel : FaceNetModel
    private val modelInfo = Models.FACENET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        faceNetModel = FaceNetModel(this, modelInfo,true,true)
        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                CAMERA_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CAMERA_PERMISSION
            )
        }
        faceAnalyser = FaceAnalyser(faceNetModel)
        setContent {
             takeImg = rememberLauncherForActivityResult(contract =  ActivityResultContracts.TakePicturePreview()){
                 image->
                if (image != null) {
                   
                    if(userName.value.isEmpty()){
                        Toast.makeText(applicationContext,"Enter a name",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        saveImage(image)
                        //val path = MediaStore.Images.Media.insertImage(applicationContext.contentResolver,image,"image",null)
                    }

                }
            }
            LazyColumn(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally){
item{
          CameraFeed(selectedCamera.value)
         Register()


        }}
        }

    }

    private fun saveImage(image: Bitmap) {
        println("SAVING")
  val directory  : File? = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES )


        if(!directory?.exists()!!)
        { println("NO EXIST")
            val userImageDirectory = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES)
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
       catch (e:Exception){
           println("ERROR SAVING FILE ${e.localizedMessage}")
       }


    }


    @Composable
     fun Register() {


        Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally ) {

            TextButton(onClick = {  takeImg.launch() }) {
                Text("Register  this face ")
            }

            TextButton(onClick = { detectFacesInFile()}) {
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

    // Get the image as a Bitmap from given Uri
    // Source -> https://developer.android.com/training/data-storage/shared/documents-files#bitmap
    fun getBitmapFromUri(contentResolver : ContentResolver, uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun detectFacesInFile() {
        val userImageFileList = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES)?.listFiles()

        if (userImageFileList != null && userImageFileList.isNotEmpty()) {

            Toast.makeText(applicationContext,"DETECTION >>> ${userImageFileList.size} images found",Toast.LENGTH_SHORT).show()

            for(userImageFile in userImageFileList) {
                println("DETECTED FILE: ${userImageFile.name}")
                val imgFileUri =  Uri.fromFile(userImageFile)
      val imageBitmap = getBitmapFromUri(contentResolver,imgFileUri)

             val image = InputImage.fromByteArray(
                 bitmapToNV21ByteArray( imageBitmap ) ,
                 imageBitmap.width,
                 imageBitmap.height,
                 0,
                 InputImage.IMAGE_FORMAT_NV21
             )


              detectFaces(image,applicationContext,userImageFile.name.dropLast(18),faceNetModel)


            }
        }
        else{
            Toast.makeText(applicationContext,"Register your face atleast once",Toast.LENGTH_SHORT).show()
            println("DETECTION  NULL")
        }

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
    @Composable
    fun CameraFeed(selectedCamera : String) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember {
            ProcessCameraProvider.getInstance(context)
        }
        //See about CameraX
        //Defining the preview view for camera
        val previewView = remember {
            PreviewView(context).apply {
                id = R.id.camera_feed
            }
        }
        val cameraExecutor = remember {
            Executors.newSingleThreadExecutor()
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center) {


//Using Androidview to compose the preview view of cameraX
            AndroidView(
                factory = { previewView }, modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            .build()
                            .also {
                                //getting the surfaceProvider for preview
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        //Telling to use the front camera,try changing according to your needs
                        val cameraLens =
                            if(selectedCamera == "Front Camera") {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }
                        else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        val faceAnalysis = ImageAnalysis.Builder()
                            //for latest image from the live preview
                            .setTargetResolution(Size( 480, 640 ) )
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, faceAnalyser)

                            }
                        try {
                            cameraProvider.unbindAll()
                            //binding camera to preview view
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraLens,


                                preview
                               
                                ,faceAnalysis
                            )
                        } catch (e: Exception) {
                            println(e.localizedMessage)
                        }


                    },
                    ContextCompat.getMainExecutor(context)
                )

            }


            CameraChooser()



        }
    }

     @Composable
    fun CameraChooser() {
        val options = listOf(
            "Front Camera",
            "Back Camera"
        )

        val onSelectionChange = { text: String ->
            selectedCamera.value = text
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            options.forEach { text ->
                Row(
                    modifier = Modifier
                        .padding(
                            all = 8.dp,
                        ),
                ) {
                    Text(
                        text = text,
                        modifier = Modifier
                            .clip(
                                shape = RoundedCornerShape(
                                    size = 12.dp,
                                ),
                            )
                            .clickable {
                                onSelectionChange(text)
                            }
                            .background(
                                if (text == selectedCamera.value) {
                                    androidx.compose.ui.graphics.Color.Magenta
                                } else {
                                    androidx.compose.ui.graphics.Color.LightGray
                                }
                            )
                            .padding(
                                vertical = 12.dp,
                                horizontal = 16.dp,
                            ),
                    )
                }
            }
        }
    }


    companion object{
const val CAMERA_PERMISSION=100
}
}
