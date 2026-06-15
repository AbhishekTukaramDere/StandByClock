package com.yourcompany.standby.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourcompany.standby.ui.screens.CropScreen
import com.yourcompany.standby.ui.screens.JournalManagementScreen
import com.yourcompany.standby.ui.screens.MainCarouselHost

@Composable
fun StandByNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = "carousel"
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("carousel") { backStackEntry ->
            val croppedPath by backStackEntry.savedStateHandle.getStateFlow<String?>("cropped_path", null).collectAsState()

            MainCarouselHost(
                onNavigateToCrop = { path ->
                    navController.navigate("crop?photoPath=$path")
                },
                croppedPath = croppedPath,
                onConsumeCropResult = {
                    backStackEntry.savedStateHandle["cropped_path"] = null
                },
                onNavigateToManagement = {
                    navController.navigate("management")
                }
            )
        }

        composable("management") { backStackEntry ->
            val croppedPath by backStackEntry.savedStateHandle.getStateFlow<String?>("cropped_path", null).collectAsState()

            JournalManagementScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCrop = { path ->
                    navController.navigate("crop?photoPath=$path")
                },
                croppedPath = croppedPath,
                onConsumeCropResult = {
                    backStackEntry.savedStateHandle["cropped_path"] = null
                }
            )
        }

        composable(
            route = "crop?photoPath={photoPath}",
            arguments = listOf(
                navArgument("photoPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val photoPath = backStackEntry.arguments?.getString("photoPath") ?: ""
            CropScreen(
                photoPath = photoPath,
                onCropped = { croppedPath ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("cropped_path", croppedPath)
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}
