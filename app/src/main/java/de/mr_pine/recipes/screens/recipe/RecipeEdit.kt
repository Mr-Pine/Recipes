package de.mr_pine.recipes.screens.recipe

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.model_views.edit.IngredientsEditCard
import de.mr_pine.recipes.model_views.edit.instructions.instructionsEditList
import de.mr_pine.recipes.model_views.view.MetaInfo
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.models.instructions.RecipeInstruction
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.clickable { /*TODO: Edit Metadata*/ }, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            recipe.metadata.title
                        )
                        IconButton(onClick = { /*TODO*/ }) {
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
            Column(modifier = Modifier.navigationBarsPadding(), horizontalAlignment = Alignment.End) {
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
                Spacer(
                    modifier = Modifier.padding(
                        bottom = try {
                            innerPadding.calculateBottomPadding() - 8.dp
                        } catch (e: java.lang.IllegalArgumentException) {
                            0.dp
                        }
                    )
                )
            }
        }
    }
}