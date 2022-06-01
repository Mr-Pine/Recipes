package de.mr_pine.recipes.viewModels

import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.Recipe

class RecipeViewModel: ViewModel() {
    var currentRecipe: Recipe? by mutableStateOf(null)
}