package com.reignscanary.privacyranger.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.reignscanary.privacyranger.CameraFeed
import com.reignscanary.privacyranger.R
import com.reignscanary.privacyranger.Register
import com.reignscanary.privacyranger.backend.FaceAnalyser
import com.reignscanary.privacyranger.backend.FaceNetModel
import com.reignscanary.privacyranger.backend.Models
import com.reignscanary.privacyranger.saveImage

var paintResource  = R.drawable.face
 var predictedName = mutableStateOf("Predicted Name will appear here")
var selectedCamera = mutableStateOf("Front Camera")
val showCamera = mutableStateOf(true)
class MainActivity : ComponentActivity() {

    lateinit var takeImg : ManagedActivityResultLauncher<Void?, Bitmap?>
    private  var userName : MutableState<String> = mutableStateOf("")
    private lateinit var faceAnalyser: FaceAnalyser
    lateinit var faceNetModel : FaceNetModel
    private val modelInfo = Models.FACENET


    override fun onStart() {
        super.onStart()
        checkPermission()
    }
    private fun checkPermission()  {

        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                CAMERA_PERMISSION
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        faceNetModel = FaceNetModel(this, modelInfo,true,true)



        faceAnalyser = FaceAnalyser(faceNetModel)
        setContent {


             takeImg = rememberLauncherForActivityResult(
                 contract =  ActivityResultContracts.TakePicturePreview()
             ){
                 image->
                if (image != null) {
                   
                    if(userName.value.isEmpty()){
                        Toast.makeText(applicationContext,"Enter a name",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        saveImage(image,applicationContext,userName)
                        //val path = MediaStore.Images.Media.insertImage(applicationContext.contentResolver,image,"image",null)
                    }

                }
            }
            LazyColumn(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                item{
                    CameraFeed(selectedCamera.value, showCamera.value,faceAnalyser)
                    Register(takeImg,userName,
                        applicationContext = applicationContext,
                        faceNetModel = faceNetModel,
                        contentResolver = contentResolver)
                }
            }
        }

    }
    companion object{
        const val CAMERA_PERMISSION=100
    }
}