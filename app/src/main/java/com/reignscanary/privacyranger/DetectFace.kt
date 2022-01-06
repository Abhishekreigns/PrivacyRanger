package com.reignscanary.privacyranger

import android.content.Context
import android.graphics.Rect
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


 var faceId = mutableStateOf(0)
 var faceShapeBounds = mutableStateOf(Rect())

 var faceShapeBoundsLeft = mutableStateOf(0)

 var faceShapeBoundsTop = mutableStateOf(0)

 var faceShapeBoundsRight = mutableStateOf(0)

 var faceShapeBoundsBottom = mutableStateOf(0)



fun detectFaces(image: InputImage,applicationContext : Context) {

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

            if(faces.size == 0){
                Toast.makeText(applicationContext,"No Faces", Toast.LENGTH_SHORT).show()


            }
            else {
                for (face in faces) {


                    faceShapeBounds.value = face.boundingBox


                    faceShapeBoundsLeft.value= face.boundingBox.left
                    faceShapeBoundsTop.value= face.boundingBox.top
                    faceShapeBoundsRight.value=  face.boundingBox.right
                    faceShapeBoundsBottom.value=face.boundingBox.bottom

                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)
                .show()

        }
}
