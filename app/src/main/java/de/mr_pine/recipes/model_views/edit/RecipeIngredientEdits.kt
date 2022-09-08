package de.mr_pine.recipes.model_views.edit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.*
import org.burnoutcrew.reorderable.*

@ExperimentalMaterial3Api
@Composable
fun RecipeIngredients.IngredientsEditCard() {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
    ) {
        var openReorderDialog by remember { mutableStateOf(false) }
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.Ingredients),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.weight(1f))
                ElevatedButton(onClick = { openReorderDialog = true }) {
                    Icon(imageVector = Icons.Default.Reorder, contentDescription = "Reorder")
                    Text(text = stringResource(R.string.Reorder))
                }
            }
            for (ingredient in ingredients) {
                ingredient.IngredientEditRow()
            }
        }

        if (openReorderDialog) {
            val ingredientsBuffer = remember(ingredients) {
                mutableStateListOf(*ingredients.toTypedArray())
            }
            AlertDialog(
                onDismissRequest = { openReorderDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        ingredients.apply {
                            clear()
                            addAll(ingredientsBuffer)
                        }
                        openReorderDialog = false
                    }) {
                        Text(stringResource(R.string.Apply))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openReorderDialog = false }) {
                        Text(stringResource(R.string.Cancel))
                    }
                },
                title = {
                    Text(text = stringResource(R.string.Reorder_Ingredients))
                },
                text = {
                    val reorderableLazyListState =
                        rememberReorderableLazyListState(onMove = { from, to ->
                            ingredientsBuffer.apply {
                                add(to.index, removeAt(from.index))
                            }
                        })
                    LazyColumn(
                        state = reorderableLazyListState.listState,
                        modifier = Modifier
                            .reorderable(reorderableLazyListState),
                    ) {
                        items(ingredientsBuffer, { it.name }) {
                            ReorderableItem(
                                reorderableState = reorderableLazyListState,
                                key = it.name
                            ) { isDragging ->
                                it.IngredientEditRow(reorderableLazyListState, isDragging)
                            }
                        }
                    }
                }
            )
        }
    }
}

private const val TAG = "RecipeIngredientEdits"

@ExperimentalMaterial3Api
@Composable
fun RecipeIngredient.IngredientEditRow(
    reorderableLazyListState: ReorderableLazyListState? = null,
    isDragging: Boolean = false
) {
    var showEditDialog by remember { mutableStateOf(false) }
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
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(if (isDragging) 5.dp else 2.dp))
            .then(
                if (reorderableLazyListState != null) Modifier.detectReorder(
                    reorderableLazyListState
                ) else Modifier.clickable { showEditDialog = true })
            .padding(horizontal = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (reorderableLazyListState != null) Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                modifier = Modifier.padding(start = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${name}: $amount ${unit.displayValue()}",
                fontSize = 20.sp
            )
            if (reorderableLazyListState == null) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }

        }
    }
    if (showEditDialog) {
        val bufferIngredient = remember { this.copy() }
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    if (with(bufferIngredient) { name.isNotEmpty() }) {
                        this.copyFrom(bufferIngredient)
                        showEditDialog = false
                    }
                }) {
                    Text(text = stringResource(id = R.string.Apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(id = R.string.Cancel))
                }
            },
            title = {
                Text(text = stringResource(R.string.Edit_ingredient))
            },
            text = {
                Column() {
                    TextField(
                        value = bufferIngredient.name,
                        onValueChange = { bufferIngredient.name = it },
                        label = {
                            Text(text = stringResource(R.string.Name))
                        },
                        isError = bufferIngredient.name.isEmpty()
                    )
                    var amountBuffer by remember { mutableStateOf(bufferIngredient.amount.toString()) }
                    Row() {
                        TextField(
                            value = amountBuffer,
                            onValueChange = { newValue ->
                                if (newValue == "") {
                                    bufferIngredient.amount = Float.NaN.amount
                                    amountBuffer = newValue
                                }
                                try {
                                    bufferIngredient.amount = newValue.toAmount()
                                    amountBuffer = newValue
                                } catch (e: NumberFormatException) {
                                    Log.w(
                                        TAG,
                                        "IngredientEditRow: Couldn't convert $newValue to IngredientAmount"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text(text = stringResource(R.string.Amount)) },
                            isError = amountBuffer.isEmpty()
                        )
                    }
                    var unitDropDownExtended by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitDropDownExtended,
                        onExpandedChange = { unitDropDownExtended = !unitDropDownExtended }) {
                        TextField(
                            readOnly = true,
                            value = bufferIngredient.unit.menuDisplayValue(),
                            onValueChange = {},
                            label = { Text("Label") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropDownExtended) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        Log.d(TAG, "IngredientEditRow: ${bufferIngredient.unit.menuDisplayValue()}")
                        ExposedDropdownMenu(
                            expanded = unitDropDownExtended,
                            onDismissRequest = { unitDropDownExtended = false }) {
                            IngredientUnit.values().forEach { selectedOption ->
                                DropdownMenuItem(
                                    text = { Text(text = selectedOption.menuDisplayValue()) },
                                    onClick = {
                                        bufferIngredient.unit =
                                            selectedOption; unitDropDownExtended = false
                                    })
                            }
                        }
                    }
                }
            }
        )
    }
}