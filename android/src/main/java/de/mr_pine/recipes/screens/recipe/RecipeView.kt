package de.mr_pine.recipes.screens.recipe

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.model_views.view.IngredientsCard
import de.mr_pine.recipes.model_views.view.MetaInfo
import de.mr_pine.recipes.model_views.view.instructionListView
import de.mr_pine.recipes.models.Recipe

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun RecipeView(
    recipe: Recipe,
    openDrawer: () -> Unit,
    toggleEditRecipe: () -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = toggleEditRecipe) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { innerPadding ->

        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyListState,
            contentPadding = innerPadding
        ) {
            item {
                recipe.metadata.MetaInfo()
            }
            item {
                recipe.ingredients.IngredientsCard()
            }

            fun setCurrentlyActiveIndex(index: Int) {
                recipe.instructions.currentlyActiveIndex = index
                /*coroutineScope.launch {
                lazyListState.animateScrollToItem(index + 2, -300)
                }*/
            }

            instructionListView(
                instructionList = recipe.instructions.instructions,
                activeIndex = recipe.instructions.currentlyActiveIndex,
                setCurrentlyActiveIndex = ::setCurrentlyActiveIndex,
                recipeTitle = recipe.metadata.title,
                getPartialIngredient = recipe.ingredients::getPartialIngredient
            )

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

