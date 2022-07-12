package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import de.mr_pine.recipes.model_views.IngredientsCard
import de.mr_pine.recipes.model_views.InstructionCard
import de.mr_pine.recipes.model_views.MetaInfo
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.viewModels.RecipeViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun RecipeView(viewModel: RecipeViewModel) {
    val currentRecipe = viewModel.recipes[viewModel.currentFileName]
    if (currentRecipe != null) {
        RecipeView(recipe = currentRecipe, openDrawer = viewModel.showNavDrawer, viewModel::loadRecipe)
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

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun RecipeView(recipe: Recipe, openDrawer: () -> Unit, loadRecipe: (Recipe) -> Unit) {
    val lazyListState = rememberLazyListState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val containerColor by TopAppBarDefaults.smallTopAppBarColors()
                .containerColor(scrollBehavior.scrollFraction)
            Box(modifier = Modifier.background(containerColor)) {
                SmallTopAppBar(
                    modifier = Modifier
                        .statusBarsPadding(),
                    title = {
                        Text(
                            recipe.metadata?.title
                                ?: "No recipe specified"
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
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { loadRecipe(recipe) }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            }
        }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                state = lazyListState
            ) {
                item {
                    recipe.metadata?.MetaInfo()
                }
                item {
                    recipe.ingredients?.IngredientsCard()
                }

                fun setCurrentlyActiveIndex(index: Int) {
                    recipe.instructions?.currentlyActiveIndex?.value = index
                    /*coroutineScope.launch {
                        lazyListState.animateScrollToItem(index + 2, -300)
                    }*/
                }

                itemsIndexed(
                    recipe.instructions?.instructions ?: listOf()
                ) { index, instruction ->
                    instruction.InstructionCard(
                        recipe.instructions?.currentlyActiveIndex?.value ?: -1,
                        setCurrentlyActiveIndex = ::setCurrentlyActiveIndex,
                        setNextActive = {
                            for ((nextIndex, next) in recipe.instructions?.instructions?.subList(
                                index + 1,
                                recipe.instructions?.instructions?.size ?: 0
                            )?.withIndex() ?: listOf()) {
                                if (!next.done) {
                                    setCurrentlyActiveIndex(nextIndex + index + 1)
                                    break
                                }
                            }
                            if (recipe.instructions?.currentlyActiveIndex?.value == index) setCurrentlyActiveIndex(
                                recipe.instructions?.instructions?.size ?: 0
                            )
                        },
                        getIngredientAbsolute = recipe.ingredients?.let { it::getPartialIngredient },
                        getIngredientFraction = recipe.ingredients?.let { it::getPartialIngredient }
                    )
                }
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

