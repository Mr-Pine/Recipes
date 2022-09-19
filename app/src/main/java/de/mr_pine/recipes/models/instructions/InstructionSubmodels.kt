package de.mr_pine.recipes.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.RecipeIngredient
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration

interface InstructionSubmodels {

    enum class EmbedTypeEnum(
        val icon: ImageVector,
        @StringRes val modelNameId: Int,
        val selectable: Boolean = true
    ) {

        UNDEFINED(Icons.Default.QuestionMark, R.string.Undefined, false),
        TIMER(Icons.Default.Timer, R.string.Timer),
        INGREDIENT(Icons.Default.Scale, R.string.Ingredient);
    }

    interface EmbedTypeModel {
        val content: String

        companion object {
            fun EmbedTypeModel.getEnum() =
                when(this) {
                    is TimerModel -> EmbedTypeEnum.TIMER
                    is IngredientModel -> EmbedTypeEnum.INGREDIENT
                    else -> EmbedTypeEnum.UNDEFINED
                }
        }
    }

    @Serializable
    @SerialName("Undefined")
    class UndefinedEmbedTypeModel : EmbedTypeModel {
        override val content = ""
    }

    @Serializable
    @SerialName("Timer")
    //TODO(With Kotlin 1.7.20, this @Contextual annotation should be unnecessary)
    class TimerModel(@Contextual val duration: Duration) : EmbedTypeModel {

        override val content = duration.toString()

        fun call(title: String, context: Context) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
        }

    }

    @Serializable
    @SerialName("Ingredient")
    class IngredientModel(
        @SerialName("ingredient_ID")
        private val ingredientId: String,
        @SerialName("display")
        val displayName: String? = null,
        @SerialName("amount_fraction")
        private val amountFraction: Float = 1f,
        @SerialName("no_amount")
        val noAmount: Boolean = false
    ) : EmbedTypeModel {

        @Transient
        var ingredient: RecipeIngredient? = null

        override val content: String
            get() = ingredient?.let { "${if (!noAmount) "${it.amount} ${it.unit.displayValue()} " else ""}${displayName ?: it.name}" }
                ?: "???"

        fun receiveIngredient(
            getIngredientFraction: ((String, Float) -> RecipeIngredient)?
        ) {
            ingredient = getIngredientFraction?.invoke(ingredientId, amountFraction)
        }

    }

}