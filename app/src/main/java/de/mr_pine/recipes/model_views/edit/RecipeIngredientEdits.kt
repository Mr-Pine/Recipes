package de.mr_pine.recipes.model_views.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.RecipeIngredient
import de.mr_pine.recipes.models.RecipeIngredients
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun RecipeIngredients.IngredientsEditCard() {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(R.string.Ingredients),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
            )
            //val list = remember { mutableStateListOf(*ingredients.toTypedArray()) }
            val reorderableLazyListState = rememberReorderableLazyListState(onMove = { from, to ->
                ingredients.apply {
                    add(to.index, removeAt(from.index))
                }
            })
            LazyColumn(
                state = reorderableLazyListState.listState,
                modifier = Modifier
                    .reorderable(reorderableLazyListState)
                    .detectReorderAfterLongPress(reorderableLazyListState),
            ) {
                items(ingredients, { it.name }) {
                    ReorderableItem(
                        reorderableState = reorderableLazyListState,
                        key = it.name
                    ) { isDragging ->
                        it.IngredientEditRow()
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeIngredient.IngredientEditRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (isChecked) 0.5f else 1f)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        Icon(imageVector = Icons.Default.Reorder, contentDescription = "Reorder")
        Text(
            text = "$name: $amount ${unit.displayValue()}",
            fontSize = 20.sp
        )
    }
}