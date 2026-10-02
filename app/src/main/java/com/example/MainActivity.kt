package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ProjectRepository
import com.example.database.WoodCraftDatabase
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.WoodCraftTheme
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WoodCraftDatabase.getInstance(applicationContext)
        val repository = ProjectRepository(database.projectDao())

        setContent {
            WoodCraftTheme {
                WoodCraftApp(
                    repository = repository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun WoodCraftApp(
    repository: ProjectRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            val projects by homeViewModel.projects.collectAsState()
            val searchQuery by homeViewModel.searchQuery.collectAsState()

            HomeScreen(
                projects = projects,
                searchQuery = searchQuery,
                onSearchQueryChange = { homeViewModel.onSearchQueryChanged(it) },
                onOpenProject = { projectId ->
                    navController.navigate("editor/$projectId")
                },
                onCreateProject = { name, unit, thickness ->
                    homeViewModel.createNewProject(name, unit, thickness) { newId ->
                        navController.navigate("editor/$newId")
                    }
                },
                onDuplicateProject = { project ->
                    homeViewModel.duplicateProject(project)
                },
                onDeleteProject = { projectId ->
                    homeViewModel.deleteProject(projectId)
                }
            )
        }

        composable(
            route = "editor/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel(
                key = projectId,
                factory = EditorViewModel.Factory(projectId, repository)
            )

            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
