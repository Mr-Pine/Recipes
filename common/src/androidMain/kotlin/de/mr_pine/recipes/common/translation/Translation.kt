package de.mr_pine.recipes.common.translation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.mr_pine.recipes.common.R

actual fun interface ITranslation {
    @Composable
    actual fun getString(): String
}

actual object Translation {
    actual val grams = ITranslation { stringResource(R.string.grams) }
    actual val kilograms = ITranslation { stringResource(R.string.kilograms) }
    actual val milliliters = ITranslation { stringResource(R.string.milliliters) }
    actual val liters = ITranslation { stringResource(R.string.liters) }
    actual val noUnit = ITranslation { stringResource(R.string.No_unit) }
    actual val undefined = ITranslation { stringResource(R.string.Undefined) }
    actual val timer = ITranslation { stringResource(R.string.Timer) }
    actual val ingredient = ITranslation { stringResource(R.string.Ingredient) }
    actual val name = ITranslation { stringResource(R.string.Name) }
    actual val amount = ITranslation { stringResource(R.string.Amount) }
    actual val unit = ITranslation { stringResource(R.string.Unit) }
    actual val type = ITranslation { stringResource(R.string.Type) }
    actual val add = ITranslation { stringResource(R.string.Add) }
    actual val duration = ITranslation { stringResource(R.string.Duration) }
    actual val displayName = ITranslation { stringResource(R.string.Display_name) }
    actual val title = ITranslation { stringResource(R.string.Title) }
    actual val portions = ITranslation { stringResource(R.string.Portions) }
    actual val author = ITranslation { stringResource(R.string.Author) }
    actual val addInstruction = ITranslation { stringResource(R.string.Add_step) }
    actual val cancel = ITranslation { stringResource(R.string.Cancel) }
    actual val delete = ITranslation { stringResource(R.string.Delete) }
    actual val deleteIngredient: ITranslation
        get() = TODO("Not yet implemented")
    actual val deleteEmbed: ITranslation
        get() = TODO("Not yet implemented")
    actual val deleteInstruction: ITranslation
        get() = TODO("Not yet implemented")
}

