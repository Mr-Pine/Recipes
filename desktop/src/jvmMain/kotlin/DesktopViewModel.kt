
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import de.mr_pine.recipes.common.models.Recipe
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
    val activeRecipe = mutableStateOf<Recipe?>(null)
    fun loadRecipe(string: String) {
        activeRecipe.value = json.decodeFromString(string)
    }
    fun loadRecipe(file: File) {
        loadRecipe(file.readText())
        activeRecipe.value?.metadata?.file = file
    }

    fun saveRecipe(window: ComposeWindow) {
        (activeRecipe.value?.metadata?.file ?: FileDialog(window, "Save recipe", FileDialog.SAVE).apply {file = "${activeRecipe.value!!.metadata.title}.rcp"; isVisible = true}.files.toSet().first() ).writeText(json.encodeToString(activeRecipe.value!!))
    }
}