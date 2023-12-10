package hu.ait.studyabroadscrapbook.ui.screen.main

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import hu.ait.studyabroadscrapbook.data.Post
import hu.ait.studyabroadscrapbook.data.PostWithId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

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

    fun uploadPlace(
        latLng: LatLng,
        placeTitle: String,
        placeText: String
    ) {
        mainUiState = MainUiState.UploadPostInProgress

        val newPlace = Post(
            uid = auth.currentUser!!.uid,
            author = auth.currentUser!!.email!!,
            title = placeTitle,
            body = placeText,
//            imgUrl = postImage,
            lat = latLng.latitude,
            lng = latLng.longitude
        )

        val postCollection = FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
        postCollection.add(newPlace).addOnSuccessListener {
            mainUiState = MainUiState.PostUploadSuccess
        }.addOnFailureListener{
            mainUiState = MainUiState.Error(it.message)
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