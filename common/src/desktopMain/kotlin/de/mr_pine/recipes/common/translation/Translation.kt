package de.mr_pine.recipes.common.translation

actual fun interface ITranslation {
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
}