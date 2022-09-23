package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
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
import de.mr_pine.recipes.viewModels.RecipeViewModel
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun RecipeView(viewModel: RecipeViewModel) {
    val currentRecipe = viewModel.currentRecipe
    if (currentRecipe != null) {
        RecipeView(
            recipe = currentRecipe,
            openDrawer = viewModel.showNavDrawer,
            viewModel::loadRecipe,
            viewModel::SaveRecipeToFile
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Missing Recipe")
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun RecipeView(
    recipe: Recipe,
    openDrawer: () -> Unit,
    loadRecipe: (Recipe) -> Unit,
    saveRecipe: (Recipe) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        recipe.metadata.title
                    )
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
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*loadRecipe(recipe)*/saveRecipe(recipe) }
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->

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
            /*item {
                recipe.ingredients.IngredientsCard()
            }*/
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
            /*

            fun setCurrentlyActiveIndex(index: Int) {
                recipe.instructions.currentlyActiveIndex = index
                /*coroutineScope.launch {
                lazyListState.animateScrollToItem(index + 2, -300)
                }*/
            }
            itemsIndexed(
                recipe.instructions.instructions
            ) { index, instruction ->
                instruction.InstructionEditCard(
                    index = index,
                    currentlyActiveIndex = recipe.instructions.currentlyActiveIndex,
                    recipeTitle = recipe.metadata.title,
                    setCurrentlyActiveIndex = ::setCurrentlyActiveIndex,
                    setNextActive = {
                        for ((nextIndex, next) in recipe.instructions.instructions.subList(
                            index + 1,
                            recipe.instructions.instructions.size
                        ).withIndex()) {
                            if (!next.done) {
                                setCurrentlyActiveIndex(nextIndex + index + 1)
                                break
                            }
                        }
                        if (recipe.instructions.currentlyActiveIndex == index) setCurrentlyActiveIndex(
                            recipe.instructions.instructions.size
                        )
                    },
                    getIngredientFraction = recipe.ingredients::getPartialIngredient,
                    ingredients = recipe.ingredients.ingredients
                )
            }*/
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

@Composable
fun ShowError(error: Exception) {
    var shown by remember {
        Log.e(
            "Recipe Error",
            "ShowError: ${error.message ?: ""}${error.stackTrace.joinToString("\n")}",
        ); mutableStateOf(true)
    }
    if (shown) AlertDialog(
        onDismissRequest = { shown = false },
        title = { Text(text = "Error Occurred") },
        text = { Text(text = error.message ?: "") },
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error"
            )
        },
        dismissButton = {
            Button(onClick = { shown = false }) {
                Text(text = "Close")
            }
        },
        confirmButton = {})
}

