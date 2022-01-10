package com.reignscanary.privacyranger

import android.Manifest
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
import java.io.FileOutputStream
import java.lang.Exception
import java.util.concurrent.Executors

var paintResource  = R.drawable.face
class MainActivity : ComponentActivity() {

    lateinit var takeImg : ManagedActivityResultLauncher<Void?, Bitmap?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setContent {
             takeImg = rememberLauncherForActivityResult(contract =  ActivityResultContracts.TakePicturePreview()){
                 image->
                if (image != null) {
                       saveImage(image)
                    //val path = MediaStore.Images.Media.insertImage(applicationContext.contentResolver,image,"image",null)


                }
            }
            LazyColumn(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally){
item{
          CameraFeed()
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

        val file = File(directory ,"user_face_image_${System.currentTimeMillis()}.jpg")


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


        Column( ) {

            TextButton(onClick = {  takeImg.launch() }) {
                Text("Register  this face ")
            }

            TextButton(onClick = { detectFacesInFile()}) {
                Text("Detect Details ")

            }


        }


    }
  private fun detectFacesInFile() {
        val userImageFileList = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES)?.listFiles()

        if (userImageFileList != null && userImageFileList.isNotEmpty()) {
            Toast.makeText(applicationContext,"DETECTION >>> ${userImageFileList.size} images found",Toast.LENGTH_SHORT).show()

            for(userImageFile in userImageFileList) {
               val imgFileUri =  Uri.fromFile(userImageFile)
             val image = InputImage.fromFilePath(applicationContext, imgFileUri)


                detectFaces(image,applicationContext)


            }
        }
        else{
            Toast.makeText(applicationContext,"Register your face atleast once",Toast.LENGTH_SHORT).show()
            println("DETECTION  NULL")
        }

    }


    @Composable
    fun CameraFeed() {
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
                        val cameraLens = CameraSelector.DEFAULT_FRONT_CAMERA
                        val faceAnalysis = ImageAnalysis.Builder()
                            //for latest image from the live preview
                            .setTargetResolution(Size( 480, 640 ) )
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, FaceAnalyser(context))

                            }
                        try {
                            cameraProvider.unbindAll()
                            //bindig camera to preview view
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraLens,
                                preview,
                                faceAnalysis
                            )
                        } catch (e: Exception) {
                            println(e.localizedMessage)
                        }


                    },
                    ContextCompat.getMainExecutor(context)
                )

            }

        }
    }


companion object{
const val CAMERA_PERMISSION=100
}
}
