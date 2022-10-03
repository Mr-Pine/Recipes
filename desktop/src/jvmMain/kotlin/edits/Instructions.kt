package edits

import FlowRow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.common.models.RecipeIngredients
import de.mr_pine.recipes.common.models.instructions.*
import de.mr_pine.recipes.common.translation.Translation
import de.mr_pine.recipes.common.views.instructions.EmbeddedText
import de.mr_pine.recipes.common.views.instructions.RecipeEmbedChip
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInstructions.InstructionList(
    setEditEmbed: (RecipeInstruction.EmbedData) -> Unit,
    editEmbed: RecipeInstruction.EmbedData?,
    setEditInstruction: (RecipeInstruction) -> Unit,
    editInstruction: RecipeInstruction?,
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
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp).border(
                            if (editInstruction == instruction) (1.5f).dp else Dp.Unspecified,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        ),
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
                                getIngredientFraction = ingredients::getPartialIngredient,
                                selectedEmbed = editEmbed,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInstruction.EditCard(
    editEmbed: RecipeInstruction.EmbedData?,
    setEditEmbed: (RecipeInstruction.EmbedData) -> Unit
) {
    ElevatedCard(modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                inlineEmbeds.forEachIndexed { index, embedData ->
                    RecipeEmbedChip(
                        onClick = { setEditEmbed(embedData) },
                        editIndex = index,
                        icon = when (embedData.embed) {
                            is InstructionSubmodels.IngredientModel -> Icons.Default.Scale
                            is InstructionSubmodels.TimerModel -> Icons.Default.Timer
                            else -> Icons.Default.QuestionMark
                        },
                        labelText = embedData.embed.content,
                        isHighlighted = embedData == editEmbed
                    )
                }
                Row(modifier = Modifier.height(32.dp)) {
                    FilterChip(
                        onClick = {
                            inlineEmbeds.add(
                                RecipeInstruction.EmbedData(
                                    true,
                                    InstructionSubmodels.UndefinedEmbedTypeModel()
                                )
                            )
                            setEditEmbed(inlineEmbeds.last())
                        },
                        label = { Text(text = Translation.add.getString()) },
                        selected = true,
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.12f
                            ),
                            selectedBorderWidth = 1.dp
                        ),
                        selectedIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = encodeInstructionString(content),
                onValueChange = { content = decodeInstructionString(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
