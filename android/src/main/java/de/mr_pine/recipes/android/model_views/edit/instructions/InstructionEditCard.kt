package de.mr_pine.recipes.android.model_views.edit.instructions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.common.model.RecipeIngredient
import de.mr_pine.recipes.common.model.instructions.InstructionSubmodels.UndefinedEmbedTypeModel
import de.mr_pine.recipes.common.model.instructions.RecipeInstruction
import de.mr_pine.recipes.common.model.instructions.decodeInstructionString
import de.mr_pine.recipes.common.model.instructions.encodeInstructionString
import de.mr_pine.recipes.common.views.instructions.EmbedTextLayout
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.InstructionEditCard(
    removeInstruction: (RecipeInstruction) -> Unit,
    ingredients: List<RecipeIngredient>,
    reorderableState: ReorderableLazyListState
) {

    val containerColor = MaterialTheme.colorScheme.let {
        if (done) it.surface.copy(alpha = 0.38f)
            .compositeOver(it.surfaceColorAtElevation(1.dp)) else it.surface
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColorFor(backgroundColor = containerColor).copy(alpha = if (done) 0.38f else 1f)
        )
    ) {

        var isEditingText by remember { mutableStateOf(content.text.isEmpty()) }


        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val instructionTextStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 22.sp,
                lineHeight = 27.sp
            )
            if (isEditingText) {

                var bufferText by remember(content) {
                    mutableStateOf(
                        encodeInstructionString(content)
                    )
                }

                Column {
                    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                        inlineEmbeds.forEachIndexed { index, embedData ->
                            embedData.RecipeEditChipStateful(
                                done = done,
                                editIndex = index,
                                inlineEmbeds::remove,
                                ingredients
                            )
                        }
                        Row(modifier = Modifier.height(32.dp)) {
                            FilterChip(
                                onClick = {
                                    inlineEmbeds.add(
                                        RecipeInstruction.EmbedData(
                                            true,
                                            UndefinedEmbedTypeModel()
                                        )
                                    )
                                },
                                label = { Text(text = stringResource(id = R.string.Add)) },
                                selected = true,
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.12f
                                    ),
                                    selectedBorderWidth = 1.dp
                                ),
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
                        value = bufferText,
                        onValueChange = {
                            bufferText = it
                        },
                        textStyle = instructionTextStyle,
                        isError = bufferText.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            if (content.text.isNotEmpty()) {
                                bufferText = encodeInstructionString(content)
                                isEditingText = false
                            } else {
                                removeInstruction(this@InstructionEditCard)
                            }
                        }) {
                            Text(stringResource(id = R.string.Cancel))
                        }
                        TextButton(onClick = {
                            if (content.text.isNotBlank()) {
                                content = decodeInstructionString(bufferText)
                                isEditingText = false
                            }
                        }) {
                            Text(text = stringResource(id = if (content.text.isBlank()) R.string.Add else R.string.Apply))
                        }
                    }
                }

            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            modifier = Modifier
                                .detectReorder(reorderableState)
                                .padding(end = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .width(0.dp)
                                .weight(1f)
                        ) {
                            EmbedTextLayout(
                                inlineEmbeds = inlineEmbeds,
                                content = content,
                                inlineEmbedContent = {
                                    it.RecipeEditChipStateful(
                                        done = done,
                                        removeEmbed = inlineEmbeds::remove,
                                        ingredients = ingredients
                                    )
                                }
                            )
                        }
                    }
                    FilledTonalButton(onClick = { isEditingText = !isEditingText }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        Text(
                            text = stringResource(id = R.string.Edit),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}