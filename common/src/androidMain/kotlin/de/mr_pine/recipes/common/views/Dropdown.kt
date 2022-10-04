package de.mr_pine.recipes.common.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun <T> DropDown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    selectedString: String,
    selectedIcon: ImageVector?,
    labelString: String,
    options: List<T>,
    optionText: @Composable (T) -> String,
    optionIcon: (T) -> ImageVector?,
    optionClick: (T) -> Boolean
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
            leadingIcon = selectedIcon?.let { { Icon(it, it.name) } },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            options.forEach {
                DropdownMenuItem(text = {
                    Row {
                        optionIcon(it)?.let {
                            Icon(it, it.name)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(optionText(it))
                    }
                }, onClick = { optionClick(it) })
            }
        }
    }
}