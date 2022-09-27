package de.mr_pine.recipes.screens.recipe

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import de.mr_pine.recipes.R
import de.mr_pine.recipes.model_views.edit.IngredientsEditCard
import de.mr_pine.recipes.model_views.edit.instructions.instructionsEditList
import de.mr_pine.recipes.model_views.view.MetaInfo
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.models.instructions.RecipeInstruction
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

private const val TAG = "RecipeEdit"

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun RecipeEdit(
    recipe: Recipe,
    openDrawer: () -> Unit,
    saveRecipe: (Recipe) -> Unit,
    toggleEditRecipe: () -> Unit
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

    var showMetadataEditDialog by remember { mutableStateOf(false) }

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
                            recipe.metadata.title
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
                SmallFloatingActionButton(onClick = { saveRecipe(recipe) }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                }
                SmallFloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val expandedFab by remember {
                    derivedStateOf {
                        reorderState.listState.firstVisibleItemIndex == 0
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { saveRecipe(recipe); toggleEditRecipe() },
                    expanded = expandedFab,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EditOff,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.Save_and_exit))
                    }
                )
            }
        }
    ) { innerPadding ->

        val metadataBuffer = remember(showMetadataEditDialog) { recipe.metadata.copy() }

        if (showMetadataEditDialog) {
            AlertDialog(
                onDismissRequest = { showMetadataEditDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        recipe.metadata.copyFrom(metadataBuffer)
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
                    Column {
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
                    }
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
                recipe.metadata.MetaInfo()
            }
            item {
                recipe.ingredients.IngredientsEditCard()
            }
            instructionsEditList(
                instructionList = recipe.instructions.instructions,
                getPartialIngredient = recipe.ingredients::getPartialIngredient,
                ingredientList = recipe.ingredients.ingredients,
                reorderableState = reorderState,
                removeInstruction = recipe.instructions.instructions::remove
            )
            item {
                FilledTonalButton(
                    onClick = {
                        recipe.instructions.instructions.add(
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