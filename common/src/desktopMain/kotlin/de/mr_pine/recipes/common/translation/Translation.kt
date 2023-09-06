package de.mr_pine.recipes.common.translation

import androidx.compose.runtime.Composable

actual fun interface ITranslation {
    @Composable
    actual fun getString(): String
}

actual object Translation {
    actual val grams = ITranslation{ "grams" }
    actual val kilograms = ITranslation{ "kilograms" }
    actual val milliliters = ITranslation{ "milliliters" }
    actual val liters = ITranslation{ "liters" }
    actual val noUnit = ITranslation{ "no Unit" }
    actual val undefined = ITranslation { "Undefined" }
    actual val timer = ITranslation { "Timer" }
    actual val ingredient = ITranslation { "Ingredient" }
    actual val name = ITranslation { "Name" }
    actual val amount = ITranslation { "Amount" }
    actual val unit = ITranslation { "Unit" }
    actual val add = ITranslation { "Add" }
    actual val type = ITranslation { "Type" }
    actual val duration = ITranslation { "Duration" }
    actual val displayName = ITranslation { "Display name" }
    actual val title = ITranslation { "Title" }
    actual val portions = ITranslation { "Portions" }
    actual val author = ITranslation { "Author" }
    actual val addInstruction = ITranslation { "Add Instruction" }
    actual val delete = ITranslation { "Delete" }
    actual val cancel = ITranslation { "Cancel" }
    actual val deleteIngredient = ITranslation { "Delete Ingredient" }
    actual val deleteEmbed = ITranslation { "Delete Embed" }
    actual val deleteInstruction = ITranslation { "Delete Instruction" }
}