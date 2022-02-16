package com.reignscanary.privacyranger

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.reignscanary.privacyranger.backend.FaceAnalyser
import com.reignscanary.privacyranger.backend.FaceNetModel
import com.reignscanary.privacyranger.backend.ModelInfo
import java.lang.Exception
import java.util.concurrent.Executors



@Composable
fun CameraFeed(selectedCamera: String, showCamera: Boolean,faceAnalyser : FaceAnalyser) {
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

                        //binding camera to preview view

                        if(showCamera) {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraLens,


                                preview, faceAnalysis
                            )
                        }
                        else{
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraLens,
                                faceAnalysis
                            )
                        }
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