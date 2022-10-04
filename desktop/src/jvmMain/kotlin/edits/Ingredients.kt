package edits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.translation.Translation
import de.mr_pine.recipes.common.views.EditColumn
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeIngredients.EditCard(editIngredient: RecipeIngredient?, setEditIngredient: (RecipeIngredient) -> Unit) {
    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
        ingredients.apply {
            add(to.index, removeAt(from.index))
        }
    })
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier.reorderable(reorderableState).padding(8.dp).clip(MaterialTheme.shapes.medium)
                .height(0.dp).weight(1f)
        ) {
            items(ingredients, { it.name }) {
                ReorderableItem(reorderableState, it.name) { isDragging ->
                    it.EditRow(isDragging, reorderableState, setEditIngredient, it == editIngredient)
                }
            }
        }
        Button(
            onClick = {
                ingredients.add(RecipeIngredient())
                setEditIngredient(ingredients.last())
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Add, "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add ingredient")
        }
    }
}

@Composable
fun RecipeIngredient.EditRow(
    isDragging: Boolean,
    reorderableState: ReorderableLazyListState,
    setEditIngredient: (RecipeIngredient) -> Unit,
    isHighlighted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .then(
                if (isDragging) Modifier.graphicsLayer(
                    scaleX = 1.1f,
                    scaleY = 1.1f,
                    transformOrigin = TransformOrigin(0f, 1f),
                    translationX = 2f,
                    translationY = 2f
                ) else Modifier
            ).border(
                if (isHighlighted) (1.5f).dp else Dp.Unspecified,
                MaterialTheme.colorScheme.primary,
                CircleShape
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .detectReorderAfterLongPress(reorderableState)
            .clickable { setEditIngredient(this) }
            .padding(horizontal = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                modifier = Modifier
                    .padding(start = 2.dp)
                    .detectReorder(reorderableState)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${name}: ${unitAmount.amount} ${unitAmount.unit.displayValue()}",
                fontSize = 20.sp,
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeIngredient.EditCard(removeIngredient: (RecipeIngredient) -> Unit) {
    ElevatedCard(modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            EditColumn { }
            DeleteButton(Translation.deleteIngredient.getString()) {
                removeIngredient(this@EditCard)
            }
        }
    }
}