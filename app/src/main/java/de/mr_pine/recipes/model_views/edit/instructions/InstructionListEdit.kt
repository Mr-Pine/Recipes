package de.mr_pine.recipes.model_views.edit.instructions

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import de.mr_pine.recipes.model.RecipeIngredient
import de.mr_pine.recipes.model.instructions.RecipeInstruction
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState

@ExperimentalMaterial3Api
fun LazyListScope.instructionsEditList(
    instructionList: List<RecipeInstruction>,
    getPartialIngredient: ((String, Float) -> RecipeIngredient)?,
    ingredientList: List<RecipeIngredient>,
    reorderableState: ReorderableLazyListState,
    removeInstruction: (RecipeInstruction) -> Unit
) {
    items(
        instructionList,
        { it.content.text }
    ) { instruction ->
        ReorderableItem(
            reorderableState = reorderableState,
            key = instruction.content.text
        ) {
            instruction.InstructionEditCard(
                getIngredientFraction = getPartialIngredient,
                ingredients = ingredientList,
                reorderableState = reorderableState,
                removeInstruction = removeInstruction
            )
        }
    }
}