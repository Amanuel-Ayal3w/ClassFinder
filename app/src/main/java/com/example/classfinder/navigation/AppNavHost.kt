package com.example.classfinder.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.classfinder.ui.feature.availability.AvailabilityViewModel
import com.example.classfinder.ui.feature.availability.FilterScreen
import com.example.classfinder.ui.feature.availability.RoomDetailsScreen
import com.example.classfinder.ui.feature.availability.ResultsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val availabilityViewModel: AvailabilityViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Filter.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Filter.route) {
            FilterScreen(
                viewModel = availabilityViewModel,
                onSearch = {
                    availabilityViewModel.computeResults()
                    navController.navigate(NavRoutes.Results.route)
                }
            )
        }
        composable(NavRoutes.Results.route) {
            ResultsScreen(
                viewModel = availabilityViewModel,
                onBack = { navController.popBackStack() },
                onOpenDetails = { roomId -> navController.navigate(NavRoutes.Details.route(roomId)) }
            )
        }
        composable(
            route = NavRoutes.Details.route,
            arguments = listOf(navArgument(NavRoutes.Details.ARG_ROOM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(NavRoutes.Details.ARG_ROOM_ID) ?: ""
            RoomDetailsScreen(
                roomId = roomId,
                viewModel = availabilityViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
