package hu.ait.studyabroadscrapbook.data

data class Post(
    var uid: String = "",
    var author: String = "",
    var title: String = "",
    var body: String = "",
    var imgUrl: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

data class PostWithId(
    val postId: String,
    val post: Post
)