package de.mr_pine.recipes.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import de.mr_pine.recipes.models.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "InstructionSubmodels"

interface InstructionSubmodels {

    data class InstructionPart(val content: String, val type: PartType) {
        enum class PartType(identifier: Int) {
            TEXT(0), EMBED(1)
        }
    }

    interface EmbedTypeModel {
        var content: String
    }

    class TimerModel(rawContent: String) : EmbedTypeModel {

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

    class IngredientModel(private val rawContent: String) : EmbedTypeModel {

        private val ingredientName: String = rawContent.extractString("Name")
        private val displayName: String = try {
            rawContent.extractString("Display")
        } catch (e: Exception) {
            Log.i(TAG, "No Displayname found "); ingredientName
        }
        private val amountRaw: String? = try {
            rawContent.extractString("Amount")
        } catch (e: Exception) {
            Log.i(TAG, "No Amount found "); null
        }
        private val noAmount: Boolean? = try {
            rawContent.extractString("NoAmount").toBoolean()
        } catch (e: Exception) {
            Log.i(TAG, "NoAmount not found "); null
        }
        var ingredient: RecipeIngredient? = null
        override var content: String
            get() = ingredient?.let { "${if(noAmount != true) "${it.amount} ${it.unit.displayValue()} " else ""}$displayName" }
                ?: "???"
            set(new) {}

        fun receiveIngredient(
            getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
            getIngredientAbsolute: ((String, IngredientAmount, IngredientUnit) -> RecipeIngredient)?
        ) {
            ingredient = when (amountRaw?.let { "@\\S+".toRegex().find(it)?.value }) {
                "@Absolute" -> {
                    val amount = amountRaw.extractString("AbsoluteAmount", '\'').toFloat().amount
                    val unit = IngredientUnit.values()
                        .find { it.identifiers.contains(amountRaw.extractString("Unit", '\'')) }
                        ?: throw Exception("unknown unit: ${amountRaw.extractString("Unit", '\'')}")
                    getIngredientAbsolute?.invoke(ingredientName, amount, unit)
                }
                "@Fraction" -> {
                    val fraction = amountRaw.extractString("Fraction", '\'').toFloat()
                    getIngredientFraction?.invoke(ingredientName, fraction)
                }
                null -> {
                    getIngredientFraction?.invoke(ingredientName, 1f)
                }
                else -> null
            }
        }

    }


    enum class EmbedType(val constructor: ((String) -> EmbedTypeModel)?) {
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