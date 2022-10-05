package de.mr_pine.recipes.android.model_views.view

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.color.MaterialColors
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.android.components.swipeabe.Swipeable
import de.mr_pine.recipes.android.ui.theme.Extended
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.models.instructions.call
import de.mr_pine.recipes.common.views.instructions.EmbeddedText

private const val TAG = "InstructionViews"

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun RecipeInstruction.InstructionCard(
    index: Int,
    currentlyActiveIndex: Int,
    recipeTitle: String,
    ingredients: List<RecipeIngredient>,
    setCurrentlyActiveIndex: (Int) -> Unit,
    setNextActive: () -> Unit,
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


    Swipeable(
        swipeRightComposable = { _, relative ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(
                        MaterialColors.layer(
                            MaterialTheme.colorScheme.surfaceVariant.toArgb(),
                            currentColor.accentContainer.toArgb(),
                            relative
                        )
                    )
                ), modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        imageVector = if (done) Icons.Default.AddTask else Icons.Default.TaskAlt,
                        contentDescription = "Done Status",
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp),
                        tint = if (done) Color.Red else currentColor.onAccentContainer
                    )
                }
            }
        },
        rightSwipedDone = {
            toggleDone(); Log.d(
            TAG,
            "InstructionCard: done: $done, index: $index, currentIndex: $currentlyActiveIndex"
        )
        }
    ) {
        val containerColor = MaterialTheme.colorScheme.let {
            if (done) it.surface.copy(alpha = 0.38f)
                .compositeOver(it.surfaceColorAtElevation(1.dp)) else it.surface
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = containerColor,
                contentColor = contentColorFor(backgroundColor = containerColor).copy(alpha = if (done) 0.38f else 1f)
            ),
            onClick = {
                setCurrentlyActiveIndex(index)
            }
        ) {

            Column(modifier = Modifier.padding(12.dp)) {
                val context = LocalContext.current

                EmbeddedText(
                    inlineEmbeds = inlineEmbeds,
                    done = done,
                    enabled = {enabled},
                    embedChipOnClick = {
                        when (it.embed) {
                            is InstructionSubmodels.IngredientModel -> it.enabled = !it.enabled
                            is InstructionSubmodels.TimerModel -> (it.embed as InstructionSubmodels.TimerModel).call(
                                recipeTitle,
                                context
                            )
                        }
                    },
                    content = content,
                    ingredients = ingredients
                )
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
}