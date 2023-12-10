package hu.ait.studyabroadscrapbook.ui.screen.main

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.ait.studyabroadscrapbook.data.Post
import hu.ait.studyabroadscrapbook.data.PostWithId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.UUID

sealed interface MainUiState {
    object Init : MainUiState
    object UploadPostInProgress : MainUiState
    object PostUploadSuccess : MainUiState
    data class PostsRetrieved(val postList: List<PostWithId>) : MainUiState
    data class Error(val error: String?) : MainUiState
}

class MainViewModel: ViewModel(){
    companion object {
        const val COLLECTION_POSTS = "posts"
    }

    var mainUiState: MainUiState by mutableStateOf(MainUiState.Init)
    private var auth: FirebaseAuth
    var currentUserId: String

    init {
        auth = Firebase.auth
        currentUserId = auth.currentUser!!.uid
    }

    fun uploadPost(
        latLng: LatLng,
        postTitle: String,
        postBody: String,
        imgUrl: String = ""
    ) {
        mainUiState = MainUiState.UploadPostInProgress

        val newPost = Post(
            uid = auth.currentUser!!.uid,
            author = auth.currentUser!!.email!!,
            title = postTitle,
            body = postBody,
            imgUrl = imgUrl,
            lat = latLng.latitude,
            lng = latLng.longitude
        )

        val postCollection = FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
        postCollection.add(newPost).addOnSuccessListener {
            mainUiState = MainUiState.PostUploadSuccess
        }.addOnFailureListener{
            mainUiState = MainUiState.Error(it.message)
        }

    }

    // upload image
    @RequiresApi(Build.VERSION_CODES.P)
    public fun uploadPostImage(
        contentResolver: ContentResolver, imageUri: Uri,
        latLng: LatLng, postTitle: String, postBody: String
    ) {
        viewModelScope.launch {
            mainUiState = MainUiState.UploadPostInProgress

            val source = ImageDecoder.createSource(contentResolver, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInBytes = baos.toByteArray()

            // prepare the empty file in the cloud
            val storageRef = FirebaseStorage.getInstance().getReference()
            val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
            val newImagesRef = storageRef.child("images/$newImage")

            // upload the jpeg byte array to the created empty file
            newImagesRef.putBytes(imageInBytes)
                .addOnFailureListener { e ->
                    mainUiState = MainUiState.Error(e.message)
                }.addOnSuccessListener { taskSnapshot ->
                    mainUiState = MainUiState.PostUploadSuccess

                    newImagesRef.downloadUrl.addOnCompleteListener(
                        object : OnCompleteListener<Uri> {
                            override fun onComplete(task: Task<Uri>) {
                                // the public URL of the image is: task.result.toString()
                                uploadPost(latLng, postTitle, postBody, task.result.toString())
                            }
                        })
                }
        }
    }


    fun postList() = callbackFlow {
        val snapshotListener =
            FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
                .addSnapshotListener() { snapshot, e ->
                    val response = if (snapshot != null) {

                        val postList = snapshot.toObjects(Post::class.java)
                        val postWithIdList = mutableListOf<PostWithId>()

                        postList.forEachIndexed { index, place ->
                            postWithIdList.add(PostWithId(snapshot.documents[index].id,
                                place))
                        }

                        MainUiState.PostsRetrieved(
                            postWithIdList
                        )
                    } else {
                        MainUiState.Error(e?.message.toString())
                    }

                    trySend(response) // emit this value through the flow
                }
        awaitClose {
            snapshotListener.remove()
        }
    }

    fun deletePost(postKey: String) {
        FirebaseFirestore.getInstance().collection(
            COLLECTION_POSTS
        ).document(postKey).delete()
    }

}