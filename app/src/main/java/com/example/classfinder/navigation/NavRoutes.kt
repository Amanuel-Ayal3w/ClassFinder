package com.example.classfinder.navigation

sealed class NavRoutes(val route: String) {
    data object Filter : NavRoutes("filter")
    data object Results : NavRoutes("results")
    data object Details : NavRoutes("details/{roomId}") {
        fun route(roomId: String) = "details/$roomId"
        const val ARG_ROOM_ID = "roomId"
    }
}
