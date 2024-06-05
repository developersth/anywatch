package com.anydev.anywatch

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anydev.anywatch.pages.ScanPage

enum class NavPage {
    HomePage,
    ScanPage
}

@Composable
fun MyNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavPage.HomePage.name) {
        composable(NavPage.HomePage.name){
            HomePage(navController)
        }
        composable(NavPage.ScanPage.name){
            ScanPage(navController)
        }
    }
}