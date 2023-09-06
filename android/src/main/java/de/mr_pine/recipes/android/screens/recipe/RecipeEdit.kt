package de.mr_pine.recipes.android.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.android.model_views.edit.IngredientsEditCard
import de.mr_pine.recipes.android.model_views.edit.instructions.instructionsEditList
import de.mr_pine.recipes.android.model_views.view.MetaInfo
import de.mr_pine.recipes.common.model.Recipe
import de.mr_pine.recipes.common.model.instructions.RecipeInstruction
import de.mr_pine.recipes.common.views.EditColumn
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@ExperimentalMaterial3Api
@Composable
fun RecipeEdit(
    recipe: Recipe,
    openDrawer: () -> Unit,
    saveRecipe: (Recipe) -> Unit,
    toggleEditRecipe: () -> Unit,
    remove: () -> Unit,
    isNew: Boolean
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val lazyListStartOffset = 2

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            recipe.instructions.instructions.add(
                to.index - lazyListStartOffset,
                recipe.instructions.instructions.removeAt(from.index - lazyListStartOffset)
            )
        }, canDragOver = {
            it.index in lazyListStartOffset..(recipe.instructions.instructions.lastIndex + lazyListStartOffset)
        }
    )

    var showMetadataEditDialog by remember { mutableStateOf(isNew) }
    val recipeBuffer = remember { recipe.copy() }

    val metadataBuffer =
        remember(showMetadataEditDialog, recipeBuffer) { recipeBuffer.metadata.copy() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    val editMetadata = {
                        showMetadataEditDialog = true
                    }

                    Row(
                        Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = editMetadata),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            recipeBuffer.metadata.title.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.Add_title)
                        )
                        IconButton(onClick = editMetadata) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Metadata"
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = openDrawer
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column(
                modifier = Modifier.navigationBarsPadding(),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(onClick = {
                    recipe.copyFrom(recipeBuffer)
                    saveRecipe(recipe)
                }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                }
                SmallFloatingActionButton(onClick = {
                    if(isNew) remove() else toggleEditRecipe()
                }) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val expandedFab by remember {
                    derivedStateOf {
                        reorderState.listState.firstVisibleItemIndex == 0
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        recipe.copyFrom(recipeBuffer)
                        saveRecipe(recipe)
                        toggleEditRecipe()
                    },
                    expanded = expandedFab,
                    icon = {
                        Icon(
                            imageVector = if(isNew) Icons.Default.Add else Icons.Default.EditOff,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(text = stringResource(if(isNew) R.string.Add_and_exit else R.string.Save_and_exit))
                    }
                )
            }
        }
    ) { innerPadding ->
        if (showMetadataEditDialog) {
            AlertDialog(
                onDismissRequest = { showMetadataEditDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        recipeBuffer.metadata.copyFrom(metadataBuffer)
                        showMetadataEditDialog = false
                    }) {
                        Text(text = stringResource(id = R.string.Apply))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMetadataEditDialog = false }) {
                        Text(stringResource(id = R.string.Cancel))
                    }
                },
                title = {
                    Text(text = stringResource(R.string.Edit_Metadata))
                },
                text = {
                    metadataBuffer.EditColumn()
                    /*Column {
                        TextField(
                            value = metadataBuffer.title,
                            onValueChange = { metadataBuffer.title = it },
                            label = {
                                Text(text = stringResource(R.string.Title))
                            },
                            isError = metadataBuffer.title.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        var portionsText by remember {
                            mutableStateOf(
                                metadataBuffer.portionSize?.toString() ?: ""
                            )
                        }
                        TextField(
                            value = portionsText,
                            //modifier = Modifier.width(90.dp),
                            onValueChange = { newValue ->
                                if (newValue == "") {
                                    metadataBuffer.portionSize = null
                                    portionsText = newValue
                                }
                                try {
                                    metadataBuffer.portionSize = newValue.toFloat()
                                    portionsText = newValue
                                } catch (e: NumberFormatException) {
                                    metadataBuffer.portionSize = Float.NaN
                                    Log.w(TAG, "RecipeEdit: Couldn't convert $newValue to Float")
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            label = { Text(text = stringResource(R.string.Portions)) },
                            isError = metadataBuffer.portionSize?.isNaN() ?: false
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = metadataBuffer.author ?: "",
                            onValueChange = {
                                metadataBuffer.author = it.takeIf { it.isNotBlank() }
                            },
                            label = {
                                Text(text = stringResource(R.string.Author))
                            }
                        )
                    }*/
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 8.dp)
                .reorderable(reorderState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = reorderState.listState
        ) {
            item {
                recipeBuffer.metadata.MetaInfo()
            }
            item {
                recipeBuffer.ingredients.IngredientsEditCard()
            }
            instructionsEditList(
                instructionList = recipeBuffer.instructions.instructions,
                ingredientList = recipeBuffer.ingredients.ingredients,
                reorderableState = reorderState,
                removeInstruction = recipeBuffer.instructions.instructions::remove
            )
            item {
                FilledTonalButton(
                    onClick = {
                        recipeBuffer.instructions.instructions.add(
                            RecipeInstruction(
                                AnnotatedString(""),
                                mutableStateListOf()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                        Text(text = stringResource(R.string.Add_step))
                    }
                }
            }

            item {
                val fabOffset = 56.dp + 24.dp + 2 * 40.dp + 16.dp
                Spacer(
                    modifier = Modifier.padding(
                        bottom = try {
                            innerPadding.calculateBottomPadding() - 8.dp
                        } catch (e: java.lang.IllegalArgumentException) {
                            0.dp
                        } + fabOffset
                    )
                )
            }
        }
    }
}