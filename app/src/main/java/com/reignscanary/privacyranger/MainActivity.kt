package com.reignscanary.privacyranger

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.reignscanary.privacyranger.ui.theme.PrivacyRangerTheme
import com.skydoves.landscapist.glide.GlideImage
import java.io.File
import java.io.IOException

var paintResource  = R.drawable.face
class MainActivity : ComponentActivity() {

        private var faceId = mutableStateOf(0)
    private var faceShapeBounds = mutableStateOf(Rect())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION)

        }

        setContent {
            var imgUriState by rememberSaveable {
                mutableStateOf(paintResource.toString().toUri())
            }
            val takeImg = rememberLauncherForActivityResult(contract =  ActivityResultContracts.TakePicturePreview()){
                if (it != null) {

                  val path = Images.Media.insertImage(this.contentResolver,it,".face",null)
                    imgUriState = Uri.parse(path)

                    imageFromPath(this, imgUriState)

                }
            }
            val selectImg = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()){
                result ->

              imgUriState =   result.data?.data!!
                imageFromPath(this,imgUriState)
            }


            PrivacyRangerTheme {
                Surface(color = MaterialTheme.colors.background)
                {
                    val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
                    galleryIntent.type = "image/*"


           LazyColumn {
               item {
                   GlideImage(
                       imageModel = imgUriState, modifier = Modifier
                           .requiredWidth(500.dp)
                           .requiredHeight(500.dp)
                           .padding(20.dp)
                   )
                   FilledTonalButton(onClick = {
                           takeImg.launch()
                           imgUriState.let {
                               val data = it
                               imgUriState = data

                           }


                       }) {
                           Text(text = "Scan  from Camera")
                       }
                         FilledTonalButton(onClick = {
                           selectImg.launch(galleryIntent)

                       }) {
                           Text(text = "Scan  from gallery")
                       }


                   }

               }


           }
                }

        }

    }

//Chosen image from files
    fun imageFromPath(context: Context, uri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)

           detectFaces(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }




    private fun detectFaces(image: InputImage) {

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                Toast.makeText(applicationContext,"Detection Successful!!",Toast.LENGTH_SHORT).show()
                if(faces.size == 0){
                    Toast.makeText(applicationContext,"No Faces",Toast.LENGTH_SHORT).show()


                }
                else {
                    for (face in faces) {

                        val bounds = face.boundingBox
                            Toast.makeText(applicationContext, "Face Shape:$bounds", Toast.LENGTH_SHORT)
                            .show()
                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                        // nose available):

                        if (face.smilingProbability != null) {
                            val smileProb = face.smilingProbability
                            if (smileProb > 0.7f) {

                                Toast.makeText(applicationContext, "That's a good smile", Toast.LENGTH_SHORT)
                                    .show()

                            } else {
                                Toast.makeText(applicationContext, "Looking Sad, What's the deal?", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        if (face.trackingId != null) {
                            val id = face.trackingId
                            faceId.value = id+1
                            Toast.makeText(applicationContext, "Face No: $id", Toast.LENGTH_SHORT)
                                .show()

                        }

                        faceShapeBounds.value = face.boundingBox


                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)
                    .show()

            }
    }
companion object{
const val CAMERA_PERMISSION=100
}
}



