package hu.ait.studyabroadscrapbook.ui.navigation

sealed class InnerNavigation(val route: String) {
    object MapView : MainNavigation("mapview")
    object ListView : MainNavigation("listview")
}