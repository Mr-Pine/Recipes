import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.models.RecipeMetadata
import de.mr_pine.recipes.common.models.instructions.RecipeInstructions
import de.mr_pine.recipes.common.models.module
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import java.awt.FileDialog
import java.io.File
import kotlin.time.Duration

class DesktopViewModel {
    private val json = Json {
        ignoreUnknownKeys = true; serializersModule =
        module + SerializersModule { contextual(Duration::class, Duration.serializer()) }
    }
    var activeRecipe = mutableStateOf<Recipe?>(null)
    fun loadRecipe(string: String) {
        activeRecipe.value = json.decodeFromString(string)
    }

    fun loadRecipe(file: File) {
        loadRecipe(file.readText())
        activeRecipe.value?.metadata?.file = file
    }

    fun saveRecipe(window: ComposeWindow, saveFile: File? = null) =
        (saveFile ?: FileDialog(window, "Save recipe", FileDialog.SAVE).apply {
            file = "${activeRecipe.value!!.metadata.title}.rcp"; isVisible = true
        }.files.toSet().takeIf { it.isNotEmpty() }
            ?.first())?.apply { writeText(json.encodeToString(activeRecipe.value!!)) }

    fun openRecipeFile(window: ComposeWindow) {
        FileDialog(window, "Open Recipe", FileDialog.LOAD).apply {
            isMultipleMode = false

            // windows
            file = listOf(".rcp").joinToString(";") { "*$it" } // e.g. '*.jpg'

            // linux
            setFilenameFilter { _, name ->
                listOf(".rcp").any {
                    name.endsWith(it)
                }
            }

            isVisible = true
        }.files.toSet().takeIf { it.isNotEmpty() }?.let {
            loadRecipe(
                it.first()
            )
        }
    }

    fun newRecipe() {
        activeRecipe.value =
            Recipe(RecipeInstructions(listOf()), RecipeMetadata("New Recipe"), RecipeIngredients(mutableStateListOf()))
    }
}