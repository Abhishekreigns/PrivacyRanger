package com.reignscanary.privacyranger.backend

import com.reignscanary.privacyranger.backend.ModelInfo


class Models {

    companion object {

        val FACENET = ModelInfo(
            "FaceNet" ,
            "mobile_face_net.tflite" ,
            0.4f ,
            10f ,
            192 ,
            112
        )

        val FACENET_512 = ModelInfo(
            "FaceNet-512" ,
            "facenet_512.tflite" ,
            0.3f ,
            23.56f ,
            512 ,
            160
        )

        val FACENET_QUANTIZED = ModelInfo(
            "FaceNet Quantized" ,
            "facenet_int_quantized.tflite" ,
            0.4f ,
            10f ,
            128 ,
            160
        )

        val FACENET_512_QUANTIZED = ModelInfo(
            "FaceNet-512 Quantized" ,
            "facenet_512_int_quantized.tflite" ,
            0.3f ,
            23.56f ,
            512 ,
            160
        )


    }

}