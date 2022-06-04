package de.mr_pine.recipes.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.models.Recipe

private const val TAG = "RecipeView"

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun RecipeView(recipe: Recipe) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        state = lazyListState
    ) {
        item {
            recipe.metadata.MetaInfo()
        }
        item {
            recipe.ingredients.IngredientsCard()
        }

        fun setCurrentlyActiveIndex(index: Int) {
            recipe.instructions.currentlyActiveIndex.value = index
            /*coroutineScope.launch {
                lazyListState.animateScrollToItem(index + 2, -300)
            }*/
        }

        itemsIndexed(recipe.instructions.instructions) { index, instruction ->
            instruction.InstructionCard(
                recipe.instructions.currentlyActiveIndex.value,
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
                    if (recipe.instructions.currentlyActiveIndex.value == index) setCurrentlyActiveIndex(
                        recipe.instructions.instructions.size
                    )
                })
        }
    }
}

@Composable
fun ShowError(errorMessage: String) {
    var shown by remember { mutableStateOf(true) }
    if (shown) AlertDialog(
        onDismissRequest = { shown = false },
        title = { Text(text = "Error Occured") },
        text = { Text(text = errorMessage)},
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

