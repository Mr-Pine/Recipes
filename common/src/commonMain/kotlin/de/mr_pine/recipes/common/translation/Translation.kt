package de.mr_pine.recipes.common.translation

import androidx.compose.runtime.Composable


expect fun interface ITranslation {
    @Composable
    fun getString(): String
}

expect object Translation {
    val grams: ITranslation
    val kilograms: ITranslation
    val milliliters: ITranslation
    val liters: ITranslation
    val noUnit: ITranslation

    val undefined: ITranslation
    val timer: ITranslation
    val ingredient: ITranslation

    val name: ITranslation
    val amount: ITranslation
    val unit: ITranslation
}