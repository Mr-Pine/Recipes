package edits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.views.instructions.IngredientEditColumn
import de.mr_pine.recipes.common.views.instructions.TimerEditColumn
import de.mr_pine.recipes.common.views.instructions.TypeDropDown
import kotlin.time.Duration.Companion.seconds

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.EmbedData.EditCard(ingredients: List<RecipeIngredient>) {

    val typeBuffers = remember(this) {
        InstructionSubmodels.EmbedTypeEnum.values().map {
            it to when (it) {
                InstructionSubmodels.EmbedTypeEnum.TIMER -> InstructionSubmodels.TimerModel(
                    mutableStateOf(0.seconds)
                )

                InstructionSubmodels.EmbedTypeEnum.UNDEFINED -> InstructionSubmodels.UndefinedEmbedTypeModel()
                InstructionSubmodels.EmbedTypeEnum.INGREDIENT -> InstructionSubmodels.IngredientModel.NO_INGREDIENT
            }
        }.toMutableStateMap()
    }

    typeBuffers[embed.getEnum()] = embed

    ElevatedCard(modifier = Modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            when (embed) {
                is InstructionSubmodels.TimerModel -> {
                    val timerEmbed = embed as InstructionSubmodels.TimerModel

                    TimerEditColumn(timerEmbed) {
                        embed = typeBuffers[it]!!
                    }
                }

                is InstructionSubmodels.IngredientModel -> {
                    val ingredientEmbed = embed as InstructionSubmodels.IngredientModel

                    IngredientEditColumn(ingredientEmbed, ingredients) {
                        embed = typeBuffers[it]!!
                    }
                }

                is InstructionSubmodels.UndefinedEmbedTypeModel -> {
                    TypeDropDown {
                        embed = typeBuffers[it]!!
                    }
                }
            }
        }
    }
}