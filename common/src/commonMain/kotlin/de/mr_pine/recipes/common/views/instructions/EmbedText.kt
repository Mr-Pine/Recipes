package de.mr_pine.recipes.common.views.instructions

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction

@Composable
fun EmbedTextLayout(
    inlineEmbeds: List<RecipeInstruction.EmbedData>,
    content: AnnotatedString,
    inlineEmbedContent: @Composable (RecipeInstruction.EmbedData) -> Unit
) {
    SubcomposeLayout { constraints ->

        val inlineContent = inlineEmbeds.mapIndexed { index, embedData ->
            val data =
                generateInlineContent(index.toString(), constraints = constraints) { inlineEmbedContent(embedData) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedText(
    inlineEmbeds: List<RecipeInstruction.EmbedData>,
    getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
    done: Boolean = false,
    enabled: RecipeInstruction.EmbedData.() -> Boolean = { true },
    embedChipOnClick: (RecipeInstruction.EmbedData) -> Unit,
    selectedEmbed: RecipeInstruction.EmbedData? = null,
    content: AnnotatedString
) {
    EmbedTextLayout(
        content = content,
        inlineEmbeds = inlineEmbeds
    ) {

        if (it.embed is InstructionSubmodels.IngredientModel && (it.embed as InstructionSubmodels.IngredientModel).let { ingredientModel -> ingredientModel.ingredient == null && ingredientModel != InstructionSubmodels.IngredientModel.NO_INGREDIENT }) {
            (it.embed as InstructionSubmodels.IngredientModel).receiveIngredient(
                getIngredientFraction
            )
        }

        val icon = when (it.embed) {
            is InstructionSubmodels.IngredientModel -> Icons.Default.Scale
            is InstructionSubmodels.TimerModel -> Icons.Default.Timer
            else -> Icons.Default.QuestionMark
        }

        RecipeEmbedChip(
            onClick = { embedChipOnClick(it) },
            selected = it.enabled(),
            enabled = !done,
            icon = icon,
            labelText = it.embed.content,
            editIndex = null,
            isHighlighted = selectedEmbed == it
        )
    }
}

fun SubcomposeMeasureScope.generateInlineContent(
    id: String,
    constraints: Constraints = Constraints(),
    content: @Composable () -> Unit
): InlineTextContent {
    val (inlineWidth, inlineHeight) = subcompose(id, content)[0].measure(constraints)
        .let { Pair(it.width.toSp(), it.height.toSp()) }

    return InlineTextContent(
        Placeholder(
            width = inlineWidth,
            height = inlineHeight,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
        )
    ) {
        content()
    }
}