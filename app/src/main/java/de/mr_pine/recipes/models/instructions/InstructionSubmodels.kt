package de.mr_pine.recipes.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import de.mr_pine.recipes.models.*
import de.mr_pine.recipes.models.Unit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class InstructionSubmodels {

    protected data class InstructionPart(val content: String, val type: PartType) {
        enum class PartType(identifier: Int) {
            TEXT(0), EMBED(1)
        }
    }

    protected interface EmbedTypeModel {
        var content: String
    }

    protected class TimerModel(rawContent: String) : EmbedTypeModel {

        val duration: Duration = rawContent.toInt().seconds
        override var content: String = duration.toString()

        fun call(title: String, context: Context) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
        }
    }

    protected class IngredientModel(private val rawContent: String) : EmbedTypeModel {

        private val ingredientName: String = rawContent.extractString("Name")
        private val displayName: String = rawContent.extractString("Display")
        private val amountRaw: String = rawContent.extractString("Amount")
        var ingredient: RecipeIngredient? = null
        override var content: String
        get() = ingredient?.let { "${it.amount} ${it.unit.displayValue()} $displayName"} ?: "???"
        set(new){}

        fun receiveIngredient(getIngredientFraction: (String, Float) -> RecipeIngredient, getIngredientAbsolute: (String, IngredientAmount, Unit) -> RecipeIngredient) {
            ingredient = when ("@\\S+".toRegex().find(amountRaw)?.value ?: "") {
                "@Absolute" -> {
                    val amount = amountRaw.extractString("AbsoluteAmount", '\'').toFloat().amount
                    val unit = Unit.values().find { it.identifiers.contains(amountRaw.extractString("Unit", '\'')) }
                        ?: throw Exception("unknown unit: ${amountRaw.extractString("Unit", '\'')}")
                    getIngredientAbsolute(ingredientName, amount, unit)
                }
                "@Fraction" -> {
                    val fraction = amountRaw.extractString("Fraction", '\'').toFloat()
                    getIngredientFraction(ingredientName, fraction)
                }
                else -> null
            }
        }

    }


    protected enum class EmbedType(val constructor: ((String) -> EmbedTypeModel)?) {
        INGREDIENT(::IngredientModel), TIMER(::TimerModel), UNKNOWN(null);

        companion object {
            operator fun get(type: String): EmbedType {
                return when (type.trim().lowercase()) {
                    "ingredient" -> INGREDIENT
                    "timer" -> TIMER
                    else -> throw Exception("Bad type: $type")
                }
            }
        }
    }
}