package de.mr_pine.recipes.models.instructions

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Constraints
import de.mr_pine.recipes.models.RecipeDeserializable
import kotlinx.serialization.Serializable

private const val TAG = "RecipeInstructions"

@Serializable
class RecipeInstructions(
    var instructions: List<RecipeInstruction>
) {

    var currentlyActiveIndex = mutableStateOf(0)

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

