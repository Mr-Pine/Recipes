package de.mr_pine.recipes.common.views

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun <T> DropDown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier.width(IntrinsicSize.Max),
    expandOnFocus: Boolean = false,
    selectedString: String,
    selectedIcon: ImageVector? = null,
    labelString: String,
    options: List<T>,
    optionText: @Composable (T) -> String,
    optionIcon: (T) -> ImageVector? = {null},
    optionClick: (T) -> Boolean
)