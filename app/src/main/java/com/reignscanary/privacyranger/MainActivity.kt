package com.reignscanary.privacyranger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception
import java.util.concurrent.Executors

var paintResource  = R.drawable.face
class MainActivity : ComponentActivity() {



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
        setContent {
        CameraFeed()

        }

    }

    @Composable
    fun CameraFeed() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember{
            ProcessCameraProvider.getInstance(context)
        }
        //See about CameraX
        //Defining the preview view for camera
        val previewView = remember {
            PreviewView(context).apply{
                id = R.id.camera_feed
            }
        }
        val cameraExecutor = remember {
            Executors.newSingleThreadExecutor()
        }
//Using Androidview to compose the preview view of cameraX
        AndroidView(factory = {previewView},modifier = Modifier.fillMaxSize()
            ){
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
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor,FaceAnalyser(context))

                        }
                    try{
                        cameraProvider.unbindAll()
                        //bindig camera to preview view
                        cameraProvider.bindToLifecycle(lifecycleOwner,cameraLens,preview,faceAnalysis)
                    }
                    catch(e:Exception){
                        println(e.localizedMessage)
                    }


                },
                ContextCompat.getMainExecutor(context)
            )

        }


    }



companion object{
const val CAMERA_PERMISSION=100
}
}
