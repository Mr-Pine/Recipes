package de.mr_pine.recipes.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.Recipe
import java.io.File
import java.io.FileFilter

class RecipeViewModel: ViewModel() {
    var currentRecipe: Recipe? by mutableStateOf(null)

    var recipeFiles: List<File> = mutableListOf()

    fun loadRecipeFiles(context: Context) {
        val recipeFolder = File(context.getExternalFilesDir(null), context.getString(R.string.externalFiles_subfolder))
        recipeFiles = recipeFolder.listFiles(FileFilter{it.extension == "rcp"})?.asList() ?: listOf()
    }
}