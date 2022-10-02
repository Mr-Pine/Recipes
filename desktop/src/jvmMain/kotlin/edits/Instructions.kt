package edits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.models.instructions.RecipeInstructions
import de.mr_pine.recipes.common.models.instructions.decodeInstructionString
import de.mr_pine.recipes.common.models.instructions.encodeInstructionString
import de.mr_pine.recipes.common.views.instructions.EmbeddedText
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInstructions.InstructionList(
    setEditEmbed: (RecipeInstruction.EmbedData) -> Unit,
    setEditInstruction: (RecipeInstruction) -> Unit,
    ingredients: RecipeIngredients
) {
    Column {
        val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
            instructions.apply {
                add(to.index, removeAt(from.index))
            }
        })
        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier.reorderable(reorderableState).padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .height(0.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(instructions, { it.content.text }) { instruction ->
                ReorderableItem(reorderableState = reorderableState, key = instruction.content.text) {
                    ElevatedCard(
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp),
                        onClick = { setEditInstruction(instruction) }) {
                        Row(
                            modifier = Modifier.padding(12.dp).detectReorderAfterLongPress(reorderableState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DragHandle, "", modifier = Modifier.detectReorder(reorderableState))
                            Spacer(modifier = Modifier.width(8.dp))
                            EmbeddedText(
                                inlineEmbeds = instruction.inlineEmbeds,
                                content = instruction.content,
                                embedChipOnClick = setEditEmbed,
                                getIngredientFraction = ingredients::getPartialIngredient
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeInstruction.EditCard() {
    TextField(value = encodeInstructionString(content), onValueChange = { content = decodeInstructionString(it) })
}
