package de.mr_pine.recipes.models.instructions

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Constraints
import de.mr_pine.recipes.models.RecipeDeserializable
import de.mr_pine.recipes.models.extractFromList

private const val TAG = "RecipeInstructions"

class RecipeInstructions(override val serialized: String, private val recipeTitle: String) :
    RecipeDeserializable {

    var instructions = mutableListOf<RecipeInstruction>()
        private set

    var currentlyActiveIndex = mutableStateOf(0)

    init {
        deserialize()
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeInstructions {
        instructions = serialized.extractFromList()
            .mapIndexed { index, serialized -> RecipeInstruction(serialized, index, recipeTitle) }
            .toMutableList()

        return this
    }

    companion object {
        const val DataTag = "Instructions"
    }
}

class RecipeInstruction(
    override val serialized: String,
    val index: Int,
    val recipeTitle: String
) : RecipeDeserializable, InstructionSubmodels {

    var content: String = serialized

    var done by mutableStateOf(false)

    init {
        content = serialized.split("\n").joinToString("\n") { it.trim() }.trim()
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeDeserializable {
        return this
    }

    data class EmbedData(var enabled: Boolean, var inlineContent: InlineTextContent? = null)

    var inlineEmbeds = mutableStateMapOf<String, EmbedData>()
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

