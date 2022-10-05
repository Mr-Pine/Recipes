
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.models.RecipeMetadata
import de.mr_pine.recipes.common.models.instructions.RecipeInstructions

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val viewModel = remember { DesktopViewModel() }

    Window(onCloseRequest = ::exitApplication, title = viewModel.activeRecipe.value?.metadata?.title ?: "", icon = painterResource("app_icon.svg")) {

        MenuBar {
            Menu("File", mnemonic = 'F') {
                if (viewModel.activeRecipe.value != null) {
                    Item(
                        "Save",
                        mnemonic = 'S',
                        shortcut = KeyShortcut(ctrl = true, key = Key.S)
                    ) { viewModel.activeRecipe.value!!.metadata.file = viewModel.saveRecipe(window, viewModel.activeRecipe.value!!.metadata.file) }
                    Item(
                        "Save As",
                        mnemonic = 'A',
                        shortcut = KeyShortcut(ctrl = true, key = Key.S, shift = true)
                    ) { viewModel.activeRecipe.value!!.metadata.file = viewModel.saveRecipe(window) }
                    Item(
                        "Open",
                        mnemonic = 'O',
                        shortcut = KeyShortcut(ctrl = true, key = Key.O)
                    ) { viewModel.openRecipeFile(window) }
                    Item(
                        "New",
                        mnemonic = 'N',
                        shortcut = KeyShortcut(ctrl = true, key = Key.N)
                    ) { viewModel.newRecipe() }
                }
            }
        }

        MaterialTheme {
            if (viewModel.activeRecipe.value == null) {
                Column {
                    Button(onClick = {
                        viewModel.openRecipeFile(window)
                    }) {
                        Text("Open Recipe")
                    }
                    Button(onClick = {
                        viewModel.activeRecipe.value = Recipe(
                            RecipeInstructions(listOf()), RecipeMetadata(""), RecipeIngredients(mutableStateListOf())
                        )
                    }) {
                        Text("New Recipe")
                    }
                }
            } else {
                MainLayout(viewModel.activeRecipe)
            }
        }


    }
}
