package edits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.translation.Translation
import de.mr_pine.recipes.common.views.instructions.IngredientEditColumn
import de.mr_pine.recipes.common.views.instructions.TimerEditColumn
import de.mr_pine.recipes.common.views.instructions.TypeDropDown
import kotlin.time.Duration.Companion.seconds

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.EmbedData.EditCard(
    ingredients: List<RecipeIngredient>,
    focusRequester: FocusRequester,
    deleteEmbed: (RecipeInstruction.EmbedData) -> Unit
) {

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


    LaunchedEffect(this, embed) {
        focusRequester.requestFocus()
    }

    ElevatedCard(modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val onTypeSelect = { it: InstructionSubmodels.EmbedTypeEnum ->
                embed = typeBuffers[it]!!
                focusRequester.requestFocus()
            }

            when (embed) {
                is InstructionSubmodels.TimerModel -> {
                    val timerEmbed = embed as InstructionSubmodels.TimerModel

                    TimerEditColumn(timerEmbed, focusRequester = focusRequester, onTypeSelect = onTypeSelect)
                }

                is InstructionSubmodels.IngredientModel -> {
                    val ingredientEmbed = embed as InstructionSubmodels.IngredientModel

                    IngredientEditColumn(ingredientEmbed, ingredients, focusRequester = focusRequester, onTypeSelect = onTypeSelect)
                }

                is InstructionSubmodels.UndefinedEmbedTypeModel -> {
                    TypeDropDown(modifier = Modifier.focusRequester(focusRequester), onSelect = onTypeSelect)
                }
            }
            DeleteButton(Translation.deleteEmbed.getString()) { deleteEmbed(this@EditCard) }
        }
    }
}