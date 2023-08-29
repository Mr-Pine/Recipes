package de.mr_pine.recipes.android.model_views.edit.instructions

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import de.mr_pine.recipes.common.model.RecipeIngredient
import de.mr_pine.recipes.common.model.instructions.RecipeInstruction
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState

@ExperimentalMaterial3Api
fun LazyListScope.instructionsEditList(
    instructionList: List<RecipeInstruction>,
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
                ingredients = ingredientList,
                reorderableState = reorderableState,
                removeInstruction = removeInstruction
            )
        }
    }
}