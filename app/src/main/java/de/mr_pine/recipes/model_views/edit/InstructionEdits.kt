package de.mr_pine.recipes.model_views.edit

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.model_views.view.generateInlineContent
import de.mr_pine.recipes.models.RecipeIngredient
import de.mr_pine.recipes.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.models.instructions.RecipeInstruction
import de.mr_pine.recipes.ui.theme.Extended

private const val TAG = "InstructionViews"

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun RecipeInstruction.InstructionEditCard(
    index: Int,
    currentlyActiveIndex: Int,
    recipeTitle: String,
    setCurrentlyActiveIndex: (Int) -> Unit,
    setNextActive: () -> Unit,
    getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
) {

    val active = currentlyActiveIndex == index


    fun toggleDone() {
        done = !done
        Log.d(
            TAG,
            "toggleDone: done: $done, index: $index, currentIndex: $currentlyActiveIndex"
        )
        if (active && done) {
            setNextActive()
        } else if (!done)
            setCurrentlyActiveIndex(index)
    }

    val currentColor = if (done) Extended.revertOrange else Extended.doneGreen


    val containerColor = MaterialTheme.colorScheme.let {
        if (done) it.surface.copy(alpha = 0.38f)
            .compositeOver(it.surfaceColorAtElevation(1.dp)) else it.surface
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColorFor(backgroundColor = containerColor).copy(alpha = if(done) 0.38f else 1f)
        ),
        onClick = {
            setCurrentlyActiveIndex(index)
        }
    ) {

        Column(modifier = Modifier.padding(12.dp)) {
            SubcomposeLayout { constraints ->

                val inlineContent = inlineEmbeds.mapIndexed { index, embedData ->
                    val data =
                        generateInlineContent(index.toString(), constraints = constraints) {

                            if (embedData.embed is InstructionSubmodels.IngredientModel && embedData.embed.ingredient == null) {
                                embedData.embed.receiveIngredient(getIngredientFraction)
                            }

                            val context = LocalContext.current

                            var enabled by remember(embedData.enabled) {
                                mutableStateOf(embedData.enabled)
                            }

                            fun setEnabled(value: Boolean) {
                                embedData.enabled = value; enabled = value
                            }

                            val icon = @Composable {
                                Icon(
                                    imageVector = when (embedData.embed) {
                                        is InstructionSubmodels.IngredientModel -> Icons.Default.Scale
                                        is InstructionSubmodels.TimerModel -> Icons.Default.Timer
                                        else -> Icons.Default.QuestionMark
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            RecipeEditChip(
                                onClick = {
                                    when (embedData.embed) {
                                        is InstructionSubmodels.IngredientModel -> setEnabled(
                                            !enabled
                                        )
                                        is InstructionSubmodels.TimerModel -> embedData.embed.call(
                                            recipeTitle,
                                            context
                                        )
                                        else -> {}
                                    }
                                },
                                selected = enabled,
                                enabled = !done,
                                icon = icon,
                                labelText = embedData.embed.content
                            )
                        }
                    index.toString() to data
                }.toMap()

                val contentPlaceable = subcompose("content") {

                    Text(
                        text = content,
                        inlineContent = inlineContent,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        )
                    )


                }[0].measure(constraints)

                layout(contentPlaceable.width, contentPlaceable.height) {
                    contentPlaceable.place(0, 0)
                }
            }
            AnimatedVisibility(
                visible = active,
                enter = scaleIn(initialScale = 0f) + expandVertically(expandFrom = Alignment.Top),
                exit = scaleOut(targetScale = 0f) + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        onClick = ::toggleDone,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = currentColor.onAccentContainer,
                            containerColor = currentColor.accentContainer
                        )
                    ) {
                        Text(
                            text = if (done) stringResource(R.string.step_repeat) else stringResource(
                                R.string.step_finish
                            )
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun RecipeEditChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean,
    icon: @Composable () -> Unit,
    labelText: String
) {
    val colors = FilterChipDefaults.elevatedFilterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSelectedContainerColor = Color.Transparent
    )

    val elevation = if (selected) FilterChipDefaults.elevatedFilterChipElevation(
        defaultElevation = 3.dp,
        pressedElevation = 3.dp,
        focusedElevation = 3.dp,
        hoveredElevation = 6.dp,
        draggedElevation = 12.dp,
        disabledElevation = 0.dp
    ) else FilterChipDefaults.elevatedFilterChipElevation()

    Box(
        modifier = modifier
            .height(30.dp)
            .clickable(onClick = onClick)
    ) {
        ElevatedFilterChip(
            onClick = onClick,
            modifier = modifier
                .padding(horizontal = 3.dp, vertical = 2.dp),
            selected = selected,
            enabled = enabled,
            leadingIcon = icon,
            label = { Text(text = labelText) },
            colors = colors,
            elevation = elevation,
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color.Transparent,
                selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                selectedBorderWidth = 1.dp
            )
        )
    }
}