package de.mr_pine.recipes.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.mr_pine.recipes.models.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

private const val TAG = "RecipeViewModel"

class RecipeViewModel(private val recipeFolder: File) : ViewModel() {
    var currentRecipe: Recipe? by mutableStateOf(null)

    var recipeFiles = mutableStateListOf<File>()

    var recipes = mutableStateListOf<Recipe>()

    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun loadRecipeFiles() {
        ioCoroutineScope.launch {
            recipeFiles.apply {
                clear()
                addAll(
                    recipeFolder.listFiles(FileFilter { it.extension == "rcp" })?.asList()
                        ?: listOf()
                )
                recipeFiles.forEach { recipeFile ->
                    if (!recipes.any { it.fileName == recipeFile.name })
                        try {
                            recipes.add(
                                Recipe(recipeFile.name, true, recipeFile.readText())
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "loadRecipeFiles: $recipeFile not properly formatted")
                        }
                }
            }
        }
    }

    fun saveRecipeFile(content: String, fileName: String) {
        val fileNameExtension = "$fileName${if (fileName.endsWith(".rcp")) "" else ".rcp"}"
        val recipeFile = File(recipeFolder, fileNameExtension)
        recipeFile.apply {
            createNewFile()
            writeText(content)
        }
        recipeFiles.indexOfFirst { it.name == fileNameExtension }
            .takeIf { it >= 0 }?.let {
                recipeFiles[it] = recipeFile
                recipes[recipes.indexOfFirst { it.fileName == fileNameExtension }] =
                    Recipe(fileNameExtension, serialized = content)
            }
    }
}

class RecipeViewModelFactory(
    private val recipeFolder: File
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(
                recipeFolder
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}