package com.lighthouse.beep.ui.page.intro.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lighthouse.beep.ui.page.intro.page.login.IntroScreen
import com.lighthouse.beep.ui.page.intro.page.permission.PermissionScreen

const val GRAPH_INTRO = "graph.intro"
const val ROUTE_LOGIN = "route.login"
const val ROUTE_PERMISSION = "route.permission"

fun NavController.navigateToIntro() {
    navigate(GRAPH_INTRO) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.introGraph() {
    navigation(
        route = GRAPH_INTRO,
        startDestination = ROUTE_LOGIN,
    ) {
        composable(
            route = ROUTE_LOGIN,
        ) {
            IntroScreen()
        }
        composable(
            route = ROUTE_PERMISSION,
        ) {
            PermissionScreen()
        }
    }
}
