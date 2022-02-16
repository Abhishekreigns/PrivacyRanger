package com.reignscanary.privacyranger

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.reignscanary.privacyranger.activities.selectedCamera
import com.reignscanary.privacyranger.activities.showCamera
import com.reignscanary.privacyranger.ui.theme.buttonOffCOlor
import com.reignscanary.privacyranger.ui.theme.buttonOnColor
import com.reignscanary.privacyranger.ui.theme.lightSurface

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
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
        modifier = Modifier.fillMaxSize()
    ) {
        options.forEach { text ->
            Row(
                modifier = Modifier
                    .padding(
                        all = 6.dp,
                    )
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
                                buttonOnColor
                            } else {
                               buttonOffCOlor
                            }
                        )
                        .padding(
                            vertical = 12.dp,
                            horizontal = 16.dp,
                        ),
                )
            }
        }
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = lightSurface),            onClick = { showCamera.value = !showCamera.value
        },modifier =Modifier.padding(top=8.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {

            Text("Show Camera Preview\n or \n run it in the background",
                modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(
                        size = 16.dp,
                    )
                ))


        }


    }
}
