package hu.ait.studyabroadscrapbook.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import hu.ait.studyabroadscrapbook.data.Post
import hu.ait.studyabroadscrapbook.ui.navigation.InnerNavigation

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableStateOf(0) }
    var innerNavController: NavHostController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Study Abroad Scrapbook:\nAIT Fall 2023"
                ) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor =
                MaterialTheme.colorScheme.secondaryContainer),
                actions = {
                    IconButton(onClick = {
                        onLogout()
                    }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(content = {
                NavigationBar(
                    //containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    NavigationBarItem(selected = selectedBottomTab == 0,
                        onClick = {
                            selectedBottomTab = 0
                            innerNavController.navigate(InnerNavigation.MapView.route) {
                                innerNavController.popBackStack()
                            }
                        },
                        label = {
                            Text(
                                text = "Map",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map",
                            )
                        })
                    NavigationBarItem(selected = selectedBottomTab == 1,
                        onClick = {
                            selectedBottomTab = 1
                            innerNavController.navigate(InnerNavigation.ListView.route) {
                                innerNavController.popBackStack()
                            }
                        },
                        label = {
                            Text(
                                text = "List",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "List",
                            )
                        })
                }
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = InnerNavigation.MapView.route
            ) {
                composable(InnerNavigation.MapView.route) {
                    MapView(mainViewModel)
                }
                composable(InnerNavigation.ListView.route) {
                    ListView(mainViewModel)
                }
            }
            when (mainViewModel.mainUiState) {
                is MainUiState.Error -> {
                    Text(text = (mainViewModel.mainUiState as MainUiState.Error).error!!)
                }

                MainUiState.Init -> {}
                MainUiState.PostUploadSuccess -> {}
                MainUiState.UploadPostInProgress -> {
                    CircularProgressIndicator()
                }

                is MainUiState.PostsRetrieved -> {}
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MapView(
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var clickedCoord by remember { mutableStateOf(LatLng(47.0, 19.0)) }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL
            )
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        uiSettings = uiSettings,
        properties = mapProperties,
        onMapClick = {
            showAdd = true
            clickedCoord = it
        }
    )
    {
        val postListState = mainViewModel.postList().collectAsState(MainUiState.Init)

        if (postListState.value is MainUiState.PostsRetrieved) {
            for (post in (postListState.value as MainUiState.PostsRetrieved).postList) {
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            post.post.lat,
                            post.post.lng
                        )
                    ),
                    title = "${post.post.title}"
                )
            }
        }
    }


    if (showAdd) {
        AddNewPostDialog(latLng = clickedCoord,
            onAddPost = { postTitle, postBody, imageUri ->
                mainViewModel.uploadPostImage(
                    context.contentResolver,
                    imageUri,
                    clickedCoord,
                    postTitle,
                    postBody)
            },
            onDialogClose = {
                showAdd = false
            })
    }

}

@Composable
fun ListView(mainViewModel: MainViewModel) {
    val postListState = mainViewModel.postList().collectAsState(MainUiState.Init)

    if (postListState.value == MainUiState.Init) {
        Text(text = "Initializing..")
    } else if (postListState.value is MainUiState.PostsRetrieved) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items((postListState.value as MainUiState.PostsRetrieved).postList) {
                PostCard(
                    post = it.post,
                    onRemoveItem = {
                        mainViewModel.deletePost(it.postId)
                    },
                    currentUserId = mainViewModel.currentUserId
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    onRemoveItem: () -> Unit = {},
    currentUserId: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.author,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .weight(9f)
                                .padding(5.dp)
                        )

                        if (currentUserId.equals(post.uid)) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                    onRemoveItem()
                                },
                                tint = Color.Red
                            )
                        }
                    }

                    if (post.imgUrl != "") {
                        AsyncImage(
                            model = post.imgUrl,
                            contentDescription = "selected image"
                        )
                    }

                    val formattedLat = String.format("%.4f", post.lat)
                    val formattedLng = String.format("%.4f", post.lng)

                    Text(
                        text = "${post.title} (${formattedLat}, ${formattedLng})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(5.dp)
                    )

                    Text(
                        text = post.body,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(5.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
