package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.service.PriceUpdateService
import com.example.service.PriceUpdateWorker
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.AdManagementScreen
import com.example.ui.screens.WidgetSettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AdViewModel
import com.example.ui.viewmodel.PriceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PriceViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Boot background sync channels
        try {
            PriceUpdateService.startService(applicationContext)
            PriceUpdateWorker.schedulePeriodicWork(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("home") {
                            MainScreen(
                                viewModel = viewModel,
                                adViewModel = adViewModel,
                                onNavigateToDetail = { itemName ->
                                    navController.navigate("detail/$itemName")
                                },
                                onNavigateToWidgetSettings = {
                                    navController.navigate("widget_settings")
                                },
                                onNavigateToAdManagement = {
                                    navController.navigate("ad_management")
                                }
                            )
                        }
                        composable(
                            route = "detail/{itemName}",
                            arguments = listOf(navArgument("itemName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
                            DetailScreen(
                                itemName = itemName,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("widget_settings") {
                            WidgetSettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("ad_management") {
                            AdManagementScreen(
                                adViewModel = adViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
