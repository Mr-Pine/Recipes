package de.mr_pine.recipes.common.views

import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.*
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun <T> DropDown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    expandOnFocus: Boolean,
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
                    ""
                )
            },
            modifier = Modifier.fillMaxWidth().expandable(expanded, { onExpandedChange(!expanded) }).onFocusChanged { if(it.hasFocus && expandOnFocus) onExpandedChange(true) },
            leadingIcon = selectedIcon?.let { { Icon(it, it.name) } }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            options.forEach {
                DropdownMenuItem(onClick = { if (optionClick(it)) onDismissRequest() }) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("ComposableModifierFactory")
@Composable
private fun Modifier.expandable(
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    menuDescription: String = "",
    expandedDescription: String = "",
    collapsedDescription: String = "",
) = pointerInput(Unit) {
    forEachGesture {
        coroutineScope {
            awaitPointerEventScope {
                var event: PointerEvent
                do {
                    event = awaitPointerEvent(PointerEventPass.Initial)
                } while (
                    !event.changes.all { it.changedToUp() }
                )
                onExpandedChange()
            }
        }
    }
}.semantics {
    stateDescription = if (expanded) expandedDescription else collapsedDescription
    contentDescription = menuDescription
    onClick {
        onExpandedChange()
        true
    }
}.onPreviewKeyEvent {
    if(it.key == Key.Enter) {
        onExpandedChange()
        true
    } else false
}