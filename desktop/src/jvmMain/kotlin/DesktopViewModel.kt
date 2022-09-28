
import androidx.compose.runtime.mutableStateOf
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.module
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
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
}