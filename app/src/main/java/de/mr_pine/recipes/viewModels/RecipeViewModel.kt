package de.mr_pine.recipes.viewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.models.module
import de.mr_pine.recipes.screens.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.pwall.json.schema.JSONSchema
import java.io.File
import java.io.FileFilter
import kotlin.time.Duration

private const val TAG = "RecipeViewModel"

class RecipeViewModel(private val recipeFolder: File, private val recipeSchema: JSONSchema) :
    ViewModel() {
    private val json = Json {
        ignoreUnknownKeys = true; serializersModule =
        module + SerializersModule { contextual(Duration::class, Duration.serializer()) }
    }

    var currentRecipe: Recipe? by mutableStateOf(null)

    var recipeFiles = mutableStateListOf<File>()

    var recipes = mutableStateListOf<Recipe>()

    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun loadRecipeFiles() {
        ioCoroutineScope.launch {
            recipeFiles = recipeFiles.apply {
                clear()
                addAll(
                    recipeFolder.listFiles(FileFilter { it.extension == "rcp" })?.asList()
                        ?: listOf()
                )
            }.distinctBy { it.name }.toMutableStateList()
            recipes = mutableStateListOf()
            recipeFiles.forEach { recipeFile ->
                getRecipeFromFile(recipeFile)?.let { recipes.add(it) }
            }
        }
    }

    fun getRecipeFromFile(file: File): Recipe? {
        val recipe = getRecipeFromString(file.readText())
        recipe?.metadata?.file = file
        return recipe
    }

    fun SaveRecipeToFile(recipe: Recipe, file: File = recipe.metadata.file!!) {
        file.parentFile?.mkdirs()
        file.apply {
            createNewFile()
            writeText(json.encodeToString(recipe))
        }
    }

    fun getRecipeFromString(raw: String): Recipe? {
        return try {
            val output = recipeSchema.validateDetailed(raw)
            if (output.valid) {
                Log.d(TAG, "getRecipeFromString: Recipe validation succeded")
                json.decodeFromString(raw)
            } else {
                Log.i(TAG, "getRecipeFromString: Recipe validation failed: ${output.error}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecipeFromString: Error while validating or decoding", e)
            null
        }
    }

    fun saveRecipeFileContent(content: String, fileName: String, loadRecipe: Boolean = false) {
        val fileNameExtension = "$fileName${if (fileName.endsWith(".rcp")) "" else ".rcp"}"
        recipeFolder.mkdirs()
        val recipeFile = File(recipeFolder, fileNameExtension)
        recipeFile.apply {
            createNewFile()
            writeText(content)
        }
        recipeFiles.indexOfFirst { it.name == fileNameExtension }
            .takeIf { it >= 0 }?.let {
                recipeFiles[it] = recipeFile
                if (loadRecipe)
                    getRecipeFromString(content)?.let { it1 -> recipes.add(it1) }
            }
    }

    fun loadRecipe(recipe: Recipe) {
        currentRecipe = recipe
    }

    fun navigateToRecipe(recipe: Recipe) {
        loadRecipe(recipe)
        navigate(Destination.RECIPE)
    }

    var navigate: (destination: Destination) -> Unit = {}
    var importRecipe: () -> Unit = {}
    var showNavDrawer: () -> Unit = {}
}

class RecipeViewModelFactory(
    private val recipeFolder: File,
    private val recipeSchema: JSONSchema
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(
                recipeFolder,
                recipeSchema
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}