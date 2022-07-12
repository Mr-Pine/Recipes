package de.mr_pine.recipes.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import de.mr_pine.recipes.models.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private const val TAG = "InstructionSubmodels"

interface InstructionSubmodels {

    @Serializable(with = EmbedSerializer::class)
    interface EmbedTypeModel {
        val content: String
    }

    @Serializable
    data class EmbedType(val type: String)

    object EmbedSerializer : KSerializer<EmbedTypeModel> {
        override val descriptor: SerialDescriptor
            get() = TODO("Not yet implemented")

        override fun deserialize(decoder: Decoder): EmbedTypeModel {
            val type = EmbedType.serializer().deserialize(decoder)
            return when (type.type) {
                "Ingredient" -> IngredientModel.serializer().deserialize(decoder)
                "Timer" -> TimerModel.serializer().deserialize(decoder)
                else -> throw Exception("No valid type: ${type.type}")
            }
        }

        override fun serialize(encoder: Encoder, value: EmbedTypeModel) {
            TODO("Not yet implemented")
        }
    }

    @Serializable
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
        override val descriptor: SerialDescriptor
            get() = TODO("Not yet implemented")

        override fun deserialize(decoder: Decoder): Duration {
            return decoder.decodeInt().seconds
        }

        override fun serialize(encoder: Encoder, value: Duration) {
            encoder.encodeInt(value.toInt(DurationUnit.SECONDS))
        }
    }

    @Serializable
    class IngredientModel(
        private val ingredientName: String,
        private val displayName: String = ingredientName,
        private val amountRaw: String? = null,
        private val noAmount: Boolean = false
    ) : EmbedTypeModel {

        @Transient
        var ingredient: RecipeIngredient? = null

        override val content: String
            get() = ingredient?.let { "${if (!noAmount) "${it.amount} ${it.unit.displayValue()} " else ""}$displayName" }
                ?: "???"

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
}