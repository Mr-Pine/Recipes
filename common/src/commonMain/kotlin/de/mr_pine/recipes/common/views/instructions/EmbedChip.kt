package de.mr_pine.recipes.common.views.instructions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
val recipeChipColors
    @Composable get() = FilterChipDefaults.elevatedFilterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledContainerColor = Color.Transparent
    )

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun recipeChipElevation(selected: Boolean) = if (selected) FilterChipDefaults.elevatedFilterChipElevation(
    defaultElevation = 3.dp,
    pressedElevation = 3.dp,
    focusedElevation = 3.dp,
    hoveredElevation = 6.dp,
    draggedElevation = 12.dp,
    disabledElevation = 0.dp
) else FilterChipDefaults.elevatedFilterChipElevation()

@ExperimentalMaterial3Api
@Composable
fun RecipeEmbedChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.clickable(onClick = onClick),
    chipModifier: Modifier = Modifier,
    selected: Boolean = true,
    enabled: Boolean = true,
    icon: ImageVector,
    labelText: String,
    isHighlighted: Boolean = false,
    editIndex: Int?
) {

    Box(
        modifier = modifier
            .height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
            if (editIndex != null)
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        if (isHighlighted) 1.5.dp else 1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.shapes.small
                    )
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            else
                Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
        ) {
            if (editIndex != null) {
                Text(
                    text = "{{${editIndex}}}",
                    modifier = Modifier.padding(horizontal = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
            ElevatedFilterChip(
                onClick = onClick,
                selected = selected,
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = { Text(text = labelText) },
                colors = recipeChipColors,
                elevation = recipeChipElevation(selected),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.Transparent,
                    selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.12f
                    ),
                    selectedBorderWidth = if (isHighlighted) 1.5.dp else 1.dp
                ),
                modifier = chipModifier
            )
        }
    }
}