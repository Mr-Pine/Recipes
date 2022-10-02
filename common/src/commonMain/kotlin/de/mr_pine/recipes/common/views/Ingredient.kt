package de.mr_pine.recipes.common.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.IngredientUnit
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.amount
import de.mr_pine.recipes.common.models.toAmount
import de.mr_pine.recipes.common.translation.Translation

@Composable
fun RecipeIngredient.EditColumn(delete: () -> Unit) {
    Column {
        TextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text(text = Translation.name.getString())
            },
            isError = name.isEmpty(),
            trailingIcon = {
                if (name.isEmpty()) {
                    Icon(Icons.Default.Delete, "delete", modifier = Modifier.clickable(onClick = delete))
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        var amountBuffer by remember { mutableStateOf(unitAmount.amount.toString()) }
        Row {
            TextField(
                value = amountBuffer,
                modifier = Modifier.width(90.dp),
                onValueChange = { newValue ->
                    if (newValue == "") {
                        unitAmount.amount = Float.NaN.amount
                        amountBuffer = newValue
                    }
                    try {
                        unitAmount.amount = newValue.toAmount()
                        amountBuffer = newValue
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text(text = Translation.amount.getString()) },
                isError = amountBuffer.isEmpty() || unitAmount.amount == 0.amount
            )
            var unitDropDownExpanded by remember { mutableStateOf(false) }
            Spacer(modifier = Modifier.width(10.dp))
            DropDown(
                expanded = unitDropDownExpanded,
                onExpandedChange = {
                    unitDropDownExpanded = it
                                   },
                onDismissRequest = { unitDropDownExpanded = false },
                selectedString = unitAmount.unit.menuDisplayValue(),
                labelString = Translation.unit.getString(),
                options = IngredientUnit.values().asList(),
                optionText = { it.menuDisplayValue() },
                optionClick = {
                    unitAmount.unit = it
                    unitDropDownExpanded = false
                }
            )
            /*ExposedDropdownMenuBox(
                expanded = unitDropDownExpanded,
                onExpandedChange = { unitDropDownExpanded = !unitDropDownExpanded }) {
                TextField(
                    readOnly = true,
                    value = unitAmount.unit.menuDisplayValue(),
                    onValueChange = {},
                    label = { Text(stringResource(id = R.string.Unit)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropDownExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = unitDropDownExpanded,
                    onDismissRequest = { unitDropDownExpanded = false }) {
                    IngredientUnit.values().forEach { selectedOption ->
                        DropdownMenuItem(
                            text = { Text(text = selectedOption.menuDisplayValue()) },
                            onClick = {
                                unitAmount.unit =
                                    selectedOption; unitDropDownExpanded = false
                            })
                    }
                }
            }*/
        }
    }
}