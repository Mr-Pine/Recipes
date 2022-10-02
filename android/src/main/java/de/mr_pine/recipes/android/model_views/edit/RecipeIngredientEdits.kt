package de.mr_pine.recipes.android.model_views.edit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.common.models.*
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
                ingredient.IngredientEditRow(deleteSelf = { ingredients.remove(ingredient) })
            }
            OutlinedButton(
                onClick = { ingredients.add(RecipeIngredient()) },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Text(text = stringResource(R.string.Add))
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
                                it.IngredientEditRow(
                                    reorderableLazyListState,
                                    isDragging = isDragging,

                                    )
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
    deleteSelf: (() -> Unit)? = null,
    isDragging: Boolean = false
) {
    val isNew by remember { mutableStateOf(this.name.isEmpty()) }
    var showEditDialog by remember { mutableStateOf(isNew) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (name.isNotBlank() && unitAmount != UnitAmount.NaN) {
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
                    if (reorderableLazyListState != null) Modifier.detectReorderAfterLongPress(
                        reorderableLazyListState
                    ) else Modifier.clickable { showEditDialog = true })
                .padding(horizontal = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (reorderableLazyListState != null) Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .detectReorder(reorderableLazyListState)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${name}: ${unitAmount.amount} ${unitAmount.unit.displayValue()}",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .width(0.dp)
                        .weight(1f)
                )
                if (reorderableLazyListState == null) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }

            }
        }
    }
    fun dismissEdit() {
        showEditDialog = false
        if (isNew && deleteSelf != null) deleteSelf()
    }

    val bufferIngredient = remember(showEditDialog) { this.copy() }
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = ::dismissEdit,
            confirmButton = {
                TextButton(onClick = {
                    if (with(bufferIngredient) { name.isNotEmpty() && unitAmount.amount != 0.amount && !unitAmount.amount.value.isNaN() }) {
                        this.copyFrom(bufferIngredient)
                        unitAmount = unitAmount.adjustUnit()
                        showEditDialog = false
                    }
                }) {
                    Text(text = stringResource(id = if (isNew) R.string.Add else R.string.Apply))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissEdit) {
                    Text(stringResource(id = R.string.Cancel))
                }
            },
            title = {
                Text(text = stringResource(if (isNew) R.string.Add_Ingredient else R.string.Edit_ingredient))
            },
            text = {
                Column {
                    TextField(
                        value = bufferIngredient.name,
                        onValueChange = {
                            bufferIngredient.name = it
                        },
                        label = {
                            Text(text = stringResource(R.string.Name))
                        },
                        isError = bufferIngredient.name.isEmpty(),
                        trailingIcon = {
                            if (bufferIngredient.name.isEmpty()) {
                                Icon(Icons.Default.Delete, "delete", modifier = Modifier.clickable {
                                    showEditDialog = false
                                    showDeleteDialog = true
                                })
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    var amountBuffer by remember { mutableStateOf(bufferIngredient.unitAmount.amount.toString()) }
                    Row {
                        TextField(
                            value = amountBuffer,
                            modifier = Modifier.width(90.dp),
                            onValueChange = { newValue ->
                                if (newValue == "") {
                                    bufferIngredient.unitAmount.amount = Float.NaN.amount
                                    amountBuffer = newValue
                                }
                                try {
                                    bufferIngredient.unitAmount.amount = newValue.toAmount()
                                    amountBuffer = newValue
                                } catch (e: NumberFormatException) {
                                    Log.w(
                                        TAG,
                                        "IngredientEditRow: Couldn't convert $newValue to IngredientAmount"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            label = { Text(text = stringResource(R.string.Amount)) },
                            isError = amountBuffer.isEmpty() || bufferIngredient.unitAmount.amount == 0.amount
                        )
                        var unitDropDownExtended by remember { mutableStateOf(false) }
                        Spacer(modifier = Modifier.width(10.dp))
                        ExposedDropdownMenuBox(
                            expanded = unitDropDownExtended,
                            onExpandedChange = { unitDropDownExtended = !unitDropDownExtended }) {
                            TextField(
                                readOnly = true,
                                value = bufferIngredient.unitAmount.unit.menuDisplayValue(),
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.Unit)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropDownExtended) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = unitDropDownExtended,
                                onDismissRequest = { unitDropDownExtended = false }) {
                                IngredientUnit.values().forEach { selectedOption ->
                                    DropdownMenuItem(
                                        text = { Text(text = selectedOption.menuDisplayValue()) },
                                        onClick = {
                                            bufferIngredient.unitAmount.unit =
                                                selectedOption; unitDropDownExtended = false
                                        })
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = { deleteSelf?.invoke(); showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.Delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(id = R.string.Cancel))
                }
            },
            title = {
                Text(text = stringResource(R.string.Delete_ingredient_title))
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(
                            stringResource(R.string.Delete_ingredient_text),
                            name
                        )
                    )
                }
            },
            icon = {
                Icon(Icons.Default.Delete, "Delete")
            }
        )
    }
}