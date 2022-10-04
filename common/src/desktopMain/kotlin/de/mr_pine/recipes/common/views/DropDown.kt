package de.mr_pine.recipes.common.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

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
    Box(modifier = modifier) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        TextField(
            interactionSource = interactionSource,
            readOnly = true,
            value = selectedString,
            onValueChange = {},
            label = { Text(labelString) },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    "",
                    modifier = Modifier.clickable { onExpandedChange(!expanded) })
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = selectedIcon?.let { { Icon(it, it.name) } }
        )

        // Horrible, but working...
        val interaction by interactionSource.interactions.collectAsState(
            FocusInteraction.Unfocus(FocusInteraction.Focus())
        )
        if (interaction is FocusInteraction.Focus) {
            onExpandedChange(!expanded)
            LocalFocusManager.current.clearFocus()
            interactionSource.tryEmit(FocusInteraction.Unfocus(FocusInteraction.Focus()))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            options.forEach {
                DropdownMenuItem(onClick = { if(optionClick(it)) onDismissRequest() }) {
                    Row {
                        optionIcon(it)?.let {
                            Icon(it, it.name)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(optionText(it))
                    }
                }
            }
        }
    }
}