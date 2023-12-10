package hu.ait.studyabroadscrapbook.ui.screen.main

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPostDialog(
    latLng: LatLng,
    onAddPost: (String, String) -> Unit,
    onDialogClose: () -> Unit = {},
) {
    var postTitle by remember { mutableStateOf("") }
    var postBody by remember { mutableStateOf("") }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val context = LocalContext.current
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }

    val pickMedia = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    Dialog(onDismissRequest = onDialogClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(size = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = "Lat: ${latLng.latitude}")
                Text(text = "Long: ${latLng.longitude}")

                OutlinedTextField(value = postTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Place title") },
                    onValueChange = {
                        postTitle = it
                    }
                )
                OutlinedTextField(value = postBody,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Description") },
                    onValueChange = {
                        postBody = it
                    }
                )

                Button(onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }) {
                    Text(text = "Pick image")
                }

                Button(onClick = {
                    onAddPost(postTitle, postBody)
                    onDialogClose()
                }) {
                    Text(text = "Add place")
                }
            }
        }
    }
}