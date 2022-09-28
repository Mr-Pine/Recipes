// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.mr_pine.recipes.common.App
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.models.RecipeMetadata
import de.mr_pine.recipes.common.models.instructions.RecipeInstructions
import java.awt.FileDialog
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val viewModel = remember { DesktopViewModel() }

    Window(onCloseRequest = ::exitApplication, title = viewModel.activeRecipe.value?.metadata?.title ?: "") {

        MenuBar {
            Menu("File", mnemonic = 'F') {
                if (viewModel.activeRecipe.value != null) Item(
                    "Save",
                    mnemonic = 'S',
                    shortcut = KeyShortcut(ctrl = true, key = Key.S)
                ) { viewModel.saveRecipe(window) }
            }
        }

        fun openFileDialog(
            window: ComposeWindow,
            title: String,
            allowedExtensions: List<String>,
            allowMultiSelection: Boolean = true
        ): Set<File> {
            return FileDialog(window, title, FileDialog.LOAD).apply {
                isMultipleMode = allowMultiSelection

                // windows
                file = allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'

                // linux
                setFilenameFilter { _, name ->
                    allowedExtensions.any {
                        name.endsWith(it)
                    }
                }

                isVisible = true
            }.files.toSet()
        }

        Column {
            App()
            Button(onClick = {
                viewModel.loadRecipe(
                    openFileDialog(window, "test", listOf(".rcp"), false).first()
                )
            }) {
                Text("test")
            }
            Button(onClick = {
                viewModel.activeRecipe.value = Recipe(
                    RecipeInstructions(listOf()), RecipeMetadata(""), RecipeIngredients(mutableStateListOf())
                )
            }) {
                Text("new")
            }
        }

    }
}
