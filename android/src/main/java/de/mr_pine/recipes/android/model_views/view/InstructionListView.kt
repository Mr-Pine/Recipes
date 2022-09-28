package de.mr_pine.recipes.android.model_views.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
fun LazyListScope.instructionListView(
    instructionList: List<RecipeInstruction>,
    activeIndex: Int,
    setCurrentlyActiveIndex: (Int) -> Unit,
    recipeTitle: String,
    getPartialIngredient: ((String, Float) -> RecipeIngredient)?
) {

    itemsIndexed(
        instructionList
    ) { index, instruction ->
        instruction.InstructionCard(
            index = index,
            currentlyActiveIndex = activeIndex,
            recipeTitle = recipeTitle,
            setCurrentlyActiveIndex = setCurrentlyActiveIndex,
            setNextActive = {
                for ((nextIndex, next) in instructionList.subList(
                    index + 1,
                    instructionList.size
                ).withIndex()) {
                    if (!next.done) {
                        setCurrentlyActiveIndex(nextIndex + index + 1)
                        break
                    }
                }
                if (activeIndex == index) setCurrentlyActiveIndex(
                    instructionList.size
                )
            },
            getIngredientFraction = getPartialIngredient
        )
    }
}