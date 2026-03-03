package com.uday.policytracker.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uday.policytracker.data.model.PolicyCategory
import com.uday.policytracker.ui.screens.DashboardScreen
import com.uday.policytracker.ui.screens.FinancialsScreen
import com.uday.policytracker.ui.screens.FolderDetailScreen
import com.uday.policytracker.ui.screens.ClosedPoliciesScreen
import com.uday.policytracker.ui.screens.PolicyDetailScreen
import com.uday.policytracker.ui.screens.PolicyFormScreen
import com.uday.policytracker.ui.screens.ServiceCategoryScreen
import com.uday.policytracker.ui.theme.PolicyTrackerTheme
import com.uday.policytracker.viewmodel.PolicyViewModel
import android.widget.Toast

@Composable
fun PolicyTrackerApp() {
    val detailRoute = "policy/detail/{id}"
    val editRoute = "policy/edit/{id}"
    val serviceRoute = "service/{category}"
    val folderRoute = "service/folder/{id}"
    val financialRoute = "financials"
    val closedPoliciesRoute = "policy/closed"

    PolicyTrackerTheme {
        val navController = rememberNavController()
        val viewModel: PolicyViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current

        val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            if (uri != null) {
                viewModel.exportBackup(uri, context.contentResolver) { success ->
                    Toast.makeText(context, if (success) "Backup saved" else "Backup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                viewModel.importBackup(uri, context.contentResolver) { success ->
                    Toast.makeText(context, if (success) "Restore completed" else "Restore failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        paddingValues = padding,
                        uiState = uiState,
                        onCategorySelected = viewModel::setCategory,
                        onOpenServiceCategory = { category ->
                            if (category == PolicyCategory.FINANCIALS) {
                                navController.navigate(financialRoute)
                            } else {
                                navController.navigate("service/${category.name}")
                            }
                        },
                        onSearchQueryChanged = viewModel::setSearchQuery,
                        onAddPolicy = { navController.navigate("policy/new") },
                        onBackup = { exportLauncher.launch("policy_tracker_backup.ptbackup.zip") },
                        onRestore = { importLauncher.launch(arrayOf("application/zip", "application/json", "text/plain", "*/*")) },
                        onOpenPolicy = { id -> navController.navigate("policy/detail/$id") },
                        onOpenFolder = { folderId -> navController.navigate("service/folder/$folderId") },
                        onOpenClosedPolicies = { navController.navigate(closedPoliciesRoute) },
                        onRenewPolicy = { policyId, input, attachments, onDone ->
                            viewModel.renewPolicy(policyId, input, attachments, context.contentResolver, onDone)
                        },
                        onDeletePolicy = viewModel::deletePolicy,
                        onClosePolicy = viewModel::closePolicy
                    )
                }
                composable(closedPoliciesRoute) {
                    ClosedPoliciesScreen(
                        paddingValues = padding,
                        closedPolicies = uiState.closedPolicies,
                        onBack = { navController.popBackStack() },
                        onOpenPolicy = { id -> navController.navigate("policy/detail/$id") }
                    )
                }
                composable(
                    route = serviceRoute,
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("category") ?: return@composable
                    val category = runCatching { PolicyCategory.valueOf(categoryName) }.getOrNull() ?: return@composable
                    ServiceCategoryScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        category = category,
                        onBack = { navController.popBackStack() },
                        onOpenFolder = { folderId -> navController.navigate("service/folder/$folderId") }
                    )
                }
                composable(financialRoute) {
                    FinancialsScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = folderRoute,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                    FolderDetailScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        folderId = id,
                        onBack = { navController.popBackStack() },
                        onOpenPolicy = { policyId -> navController.navigate("policy/detail/$policyId") }
                    )
                }
                composable(
                    route = detailRoute,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                    PolicyDetailScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        policyId = id,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate("policy/edit/$id") }
                    )
                }
                composable("policy/new") {
                    PolicyFormScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        policyId = null,
                        onBack = { navController.popBackStack() },
                        onSaved = { id ->
                            navController.navigate("policy/detail/$id") {
                                popUpTo("policy/new") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = editRoute,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                    PolicyFormScreen(
                        paddingValues = padding,
                        viewModel = viewModel,
                        policyId = id,
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.navigate("policy/detail/$id") {
                                popUpTo("policy/edit/$id") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
