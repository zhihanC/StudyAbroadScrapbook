package hu.ait.studyabroadscrapbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import hu.ait.studyabroadscrapbook.ui.navigation.MainNavigation
import hu.ait.studyabroadscrapbook.ui.screen.login.LoginScreen
import hu.ait.studyabroadscrapbook.ui.screen.main.MainScreen
import hu.ait.studyabroadscrapbook.ui.theme.StudyAbroadScrapbookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyAbroadScrapbookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavHost()
                }
            }
        }
    }
}

@Composable
fun MainAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController,
        startDestination = MainNavigation.LoginScreen.route) {

        composable(MainNavigation.LoginScreen.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(MainNavigation.MainScreen.route)
            })
        }
        composable(MainNavigation.MainScreen.route) {
            MainScreen(onLogout = {
                navController.navigate(MainNavigation.LoginScreen.route)
            })
        }
    }

}