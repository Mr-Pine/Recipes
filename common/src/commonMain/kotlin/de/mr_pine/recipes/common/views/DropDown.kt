package de.mr_pine.recipes.common.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun <T> DropDown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    selectedString: String,
    labelString: String,
    options: List<T>,
    optionText: @Composable (T) -> String,
    optionClick: (T) -> Unit
)