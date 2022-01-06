package com.reignscanary.privacyranger

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.reignscanary.privacyranger.ui.theme.PrivacyRangerTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import java.io.IOException

var paintResource  = R.drawable.face
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION)

        }

            val faceDatabase = FaceDatabase.getInstance(applicationContext)
            val list = faceDatabase.faceDao().faceList()

        setContent {
            var imgUriState by rememberSaveable {
                mutableStateOf(paintResource.toString().toUri())
            }
            val takeImg = rememberLauncherForActivityResult(contract =  ActivityResultContracts.TakePicturePreview()){
                if (it != null) {

                  val path = Images.Media.insertImage(applicationContext.contentResolver,it,".face",null)
                    imgUriState = Uri.parse(path)

                    imageFromPath(applicationContext, imgUriState)

                }
            }
            val selectImg = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()){
                result ->

              imgUriState =   result.data?.data!!
                imageFromPath(applicationContext,imgUriState)
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

                   Text(text = "Face Shape Bounds:${faceShapeBounds.value}")
                   Spacer(modifier = Modifier)
                   Text(text = "Face Shape Left ${faceShapeBoundsLeft.value}")
                   Spacer(modifier = Modifier)

                   Text(text = "Face Shape Top ${faceShapeBoundsTop.value}")
                   Spacer(modifier = Modifier)
                   Text(text = "Face Shape Right ${faceShapeBoundsRight.value}")
                   Spacer(modifier = Modifier)

                   Text(text = "Face Shape Bottom ${faceShapeBoundsBottom.value}")
               }



               }


           }
                }

        }}

    }

//Chosen image from files
    fun imageFromPath(context: Context, uri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)

           detectFaces(image,context)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }




companion object{
const val CAMERA_PERMISSION=100
}
}



