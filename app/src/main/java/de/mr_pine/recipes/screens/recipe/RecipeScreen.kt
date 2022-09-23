package de.mr_pine.recipes.screens.recipe

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.mr_pine.recipes.viewModels.RecipeViewModel

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun RecipeScreen(viewModel: RecipeViewModel) {
    val currentRecipe = viewModel.currentRecipe
    var editRecipe by remember { mutableStateOf(false) }
    fun setEditMode(value: Boolean = !editRecipe) {
        editRecipe = value
    }
    if (currentRecipe != null) {
        if (editRecipe) {
            RecipeEdit(
                recipe = currentRecipe,
                openDrawer = viewModel.showNavDrawer,
                viewModel::saveRecipeToFile,
                toggleEditRecipe = ::setEditMode
            )
        } else {
            RecipeView(
                recipe = currentRecipe,
                openDrawer = viewModel.showNavDrawer,
                toggleEditRecipe = ::setEditMode
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Missing Recipe")
        }
    }
}