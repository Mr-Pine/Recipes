package de.mr_pine.recipes.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.mr_pine.recipes.screens.Destination.HOME
import de.mr_pine.recipes.screens.Destination.RECIPE
import de.mr_pine.recipes.screens.recipe.RecipeScreen
import de.mr_pine.recipes.viewModels.RecipeViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun RecipeNavHost(navController: NavHostController, viewModel: RecipeViewModel) {
    NavHost(navController = navController, startDestination = HOME.toString()) {
        composable(HOME.toString()) {
            Home(viewModel = viewModel)
        }
        composable(RECIPE.toString()) {
            RecipeScreen(viewModel = viewModel)
        }
    }
}

enum class Destination{
    HOME, RECIPE
}