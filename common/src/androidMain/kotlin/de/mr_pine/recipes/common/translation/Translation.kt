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
}

