
package com.reignscanary.privacyranger
/*
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage

var imgUriState by rememberSaveable {
    mutableStateOf(paintResource.toString().toUri())
}
val takeImg = rememberLauncherForActivityResult(contract =  ActivityResultContracts.TakePicturePreview()){
    if (it != null) {

        val path = MediaStore.Images.Media.insertImage(applicationContext.contentResolver,it,".face",null)
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


 */

 */
