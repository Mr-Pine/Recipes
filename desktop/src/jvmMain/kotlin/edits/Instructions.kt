package edits

import FlowRow
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.common.models.RecipeIngredient
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
    ingredients: List<RecipeIngredient>,
    setEditInstruction: (RecipeInstruction) -> Unit,
    editInstruction: RecipeInstruction?,
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
                .clip(MaterialTheme.shapes.medium).fillMaxWidth()
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
                                selectedEmbed = editEmbed,
                                ingredients = ingredients,
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
        Button(modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally), onClick = {
            instructions.add(RecipeInstruction(AnnotatedString(""), listOf()))
            setEditInstruction(instructions.last())
        }) {
            Icon(Icons.Default.Add, "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translation.addInstruction.getString())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeInstruction.EditCard(
    editEmbed: RecipeInstruction.EmbedData?,
    focusRequester: FocusRequester,
    ingredients: List<RecipeIngredient>,
    setEditEmbed: (RecipeInstruction.EmbedData) -> Unit,
    deleteInstruction: (RecipeInstruction) -> Unit
) {
    ElevatedCard(modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            var contentValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        AnnotatedString(encodeInstructionString(content)),
                        TextRange(Int.MAX_VALUE)
                    )
                )
            }

            FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                val onExtraClick = { index: Int ->
                    val newText = contentValue.let {
                        it.text.replaceRange(
                            it.selection.start.coerceAtMost(it.selection.end),
                            it.selection.end.coerceAtLeast(it.selection.start),
                            "{{$index}}"
                        )
                    }
                    content = decodeInstructionString(newText)
                    contentValue = contentValue.copy(
                        text = newText,
                        selection = TextRange(contentValue.selection.start + 4 + index.toString().length)
                    )
                }
                inlineEmbeds.forEachIndexed { index, embedData ->
                    RecipeEmbedChip(
                        onClick = { setEditEmbed(embedData) },
                        modifier = Modifier
                            .onClick(
                                onClick = {
                                    onExtraClick(index)
                                    setEditEmbed(embedData)
                                }
                            )
                            .onClick(
                                matcher = PointerMatcher.mouse(
                                    PointerButton.Secondary
                                ),
                                onClick = { onExtraClick(index) }
                            ),
                        editIndex = index,
                        icon = when (embedData.embed) {
                            is InstructionSubmodels.IngredientModel -> Icons.Default.Scale
                            is InstructionSubmodels.TimerModel -> Icons.Default.Timer
                            else -> Icons.Default.QuestionMark
                        },
                        labelText = embedData.embed.content(ingredients),
                        isHighlighted = embedData == editEmbed,
                    )
                }
                Row(modifier = Modifier.height(32.dp)) {
                    val addEmbed = {
                        inlineEmbeds.add(
                            RecipeInstruction.EmbedData(
                                true,
                                InstructionSubmodels.UndefinedEmbedTypeModel()
                            )
                        )
                        setEditEmbed(inlineEmbeds.last())
                    }
                    FilterChip(
                        onClick = addEmbed,
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
                        },
                        modifier = Modifier.onClick(
                            matcher = PointerMatcher.mouse(
                                PointerButton.Secondary
                            ),
                            onClick = {
                                addEmbed()
                                onExtraClick(inlineEmbeds.lastIndex)
                            }
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = contentValue,
                onValueChange = {
                    content = decodeInstructionString(it.text)
                    contentValue = it
                },
                placeholder = { Text("Instruction") },
                modifier = Modifier.fillMaxWidth().moveFocusOnTab().focusRequester(focusRequester)
            )
            DeleteButton(Translation.deleteInstruction.getString()) {
                deleteInstruction(this@EditCard)
            }
        }
    }
}


//Needed because of https://github.com/JetBrains/compose-jb/issues/109
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.moveFocusOnTab(
    focusManager: FocusManager = LocalFocusManager.current
) = onPreviewKeyEvent {
    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
        focusManager.moveFocus(
            if (it.isShiftPressed) FocusDirection.Previous
            else FocusDirection.Next
        )
        return@onPreviewKeyEvent true
    }
    false
}