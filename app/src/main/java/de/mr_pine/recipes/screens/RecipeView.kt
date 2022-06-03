package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.models.Recipe

private const val TAG = "RecipeView"

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun RecipeView(recipe: Recipe) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            recipe.metadata.MetaInfo()
        }
        item {
            recipe.ingredients.IngredientsCard()
        }
        items(recipe.instructions.instructions) { instruction ->
            instruction.InstructionCard()
        }
    }
}

