package com.example.wallmeasure.navigation

sealed class Screen(val route: String, val title: String) {
    data object Splash : Screen("splash", "WallMeasure")
    data object Home : Screen("home", "Home")
    data object Openings : Screen("openings", "Openings")
    data object MultipleWalls : Screen("multiple_walls", "Multiple Walls")
    data object History : Screen("history", "History")
    data object Settings : Screen("settings", "Settings")
    data object About : Screen("about", "About")
}
