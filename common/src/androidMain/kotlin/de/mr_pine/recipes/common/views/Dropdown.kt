package de.mr_pine.recipes.common.views

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun <T> DropDown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    selectedString: String,
    labelString: String,
    options: List<T>,
    optionText: @Composable (T) -> String,
    optionClick: (T) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedString,
            onValueChange = {},
            label = { Text(labelString) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            options.forEach {
                DropdownMenuItem(text = { Text(optionText(it)) }, onClick = { optionClick(it) })
            }
        }
    }
}