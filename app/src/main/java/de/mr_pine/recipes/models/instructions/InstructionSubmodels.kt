package de.mr_pine.recipes.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import de.mr_pine.recipes.models.RecipeIngredient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private const val TAG = "InstructionSubmodels"

interface InstructionSubmodels {


    interface EmbedTypeModel {
        val content: String
    }

    @Serializable
    @SerialName("Timer")
    class TimerModel(@Serializable(with = SecondsSerializer::class) val duration: Duration) : EmbedTypeModel {

        override val content: String = duration.toString()

        fun call(title: String, context: Context) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
        }
    }

    object SecondsSerializer: KSerializer<Duration> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.FLOAT)

        override fun deserialize(decoder: Decoder): Duration {
            return decoder.decodeInt().seconds
        }

        override fun serialize(encoder: Encoder, value: Duration) {
            encoder.encodeInt(value.toInt(DurationUnit.SECONDS))
        }
    }

    @Serializable
    @SerialName("Ingredient")
    class IngredientModel(
        @SerialName("name")
        private val ingredientName: String,
        @SerialName("display")
        private val displayName: String = ingredientName,
        @SerialName("amount_fraction")
        private val amountFraction: Float = 1f,
        @SerialName("no_amount")
        private val noAmount: Boolean = false
    ) : EmbedTypeModel {

        @Transient
        var ingredient: RecipeIngredient? = null

        override val content: String
            get() = ingredient?.let { "${if (!noAmount) "${it.amount} ${it.unit.displayValue()} " else ""}$displayName" }
                ?: "???"

        fun receiveIngredient(
            getIngredientFraction: ((String, Float) -> RecipeIngredient)?
        ) {
            ingredient = getIngredientFraction?.invoke(ingredientName, amountFraction)
        }

    }
}