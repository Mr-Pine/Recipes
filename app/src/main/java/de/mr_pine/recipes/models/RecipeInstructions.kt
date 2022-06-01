package de.mr_pine.recipes.models

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp

class RecipeInstructions(override val serialized: String) : RecipeDeserializable {

    var instructions = mutableListOf<RecipeInstruction>()
        private set

    init {
        deserialize()
    }

    override fun deserialize(): RecipeInstructions {
        instructions = serialized.extractFromList().map { RecipeInstruction(it) }.toMutableList()

        return this
    }

    companion object {
        const val DataTag = "Instructions"
    }
}

class RecipeInstruction(
    override val serialized: String

) : RecipeDeserializable {
    override fun deserialize(): RecipeDeserializable {
        return this
    }

    @ExperimentalMaterial3Api
    @Composable
    fun InstructionCard() {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = serialized)
            }
        }
    }
}