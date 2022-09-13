package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.model_views.edit.IngredientsEditCard
import de.mr_pine.recipes.model_views.edit.InstructionEditCard
import de.mr_pine.recipes.model_views.view.MetaInfo
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.viewModels.RecipeViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@ExperimentalAnimationApi
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
    val lazyListState = rememberLazyListState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
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
            ExtendedFloatingActionButton(
                onClick = { /*loadRecipe(recipe)*/saveRecipe(recipe) }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = /*PaddingValues(bottom = 16.dp)*/ innerPadding,
            state = lazyListState
        ) {
            item {
                recipe.metadata.MetaInfo()
            }
            item {
                //recipe.ingredients.IngredientsCard()
            }
            item {
                recipe.ingredients.IngredientsEditCard()
            }

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
                    getIngredientFraction = recipe.ingredients::getPartialIngredient
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

