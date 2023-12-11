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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import hu.ait.studyabroadscrapbook.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPostDialog(
    latLng: LatLng,
    onAddPost: (String, String, Uri) -> Unit,
    onDialogClose: () -> Unit = {},
) {
    var postTitle by remember { mutableStateOf("") }

    var titleErrorText by rememberSaveable {
        mutableStateOf("")
    }

    var titleInputErrorState by rememberSaveable {
        mutableStateOf(false)
    }

    var postBody by remember { mutableStateOf("") }

    var bodyErrorText by rememberSaveable {
        mutableStateOf("")
    }

    var bodyInputErrorState by rememberSaveable {
        mutableStateOf(false)
    }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val pickMedia = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }



    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var context = LocalContext.current

    Dialog(
        onDismissRequest = onDialogClose

    ) {
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
                    label = { Text(text = stringResource(R.string.title)) },
                    isError = titleInputErrorState,
                    onValueChange = {
                        titleInputErrorState = false
                        postTitle = it
                    },
                    supportingText = {
                        if (titleInputErrorState)
                            Text(
                                text = titleErrorText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                    },
                    trailingIcon = {
                        if (titleInputErrorState) {
                            Icon(
                                Icons.Filled.Warning, "error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(value = postBody,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bodyInputErrorState,
                    label = { Text(text = stringResource(R.string.description)) },
                    onValueChange = {
                        bodyInputErrorState = false
                        postBody = it
                    },
                    supportingText = {
                    if (bodyInputErrorState)
                        Text(
                            text = bodyErrorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                },
                trailingIcon = {
                    if (bodyInputErrorState) {
                        Icon(
                            Icons.Filled.Warning, "error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                })

                Button(onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }) {
                    Text(text = stringResource(R.string.pick_image))
                }

                Button(onClick = {
                    if (postTitle.isEmpty()) {
                        titleErrorText = context.getString(R.string.post_title_cannot_be_empty)
                        titleInputErrorState = true
                    } else if (postBody.isEmpty()) {
                        bodyErrorText = context.getString(R.string.post_description_cannot_be_empty)
                        bodyInputErrorState = true
                    } else if (imageUri == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.please_select_an_image))
                        }
                    } else {
                        onAddPost(postTitle, postBody, imageUri!!)
                        onDialogClose()
                    }
                }) {
                    Text(text = stringResource(R.string.add_place))
                }
            }
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}