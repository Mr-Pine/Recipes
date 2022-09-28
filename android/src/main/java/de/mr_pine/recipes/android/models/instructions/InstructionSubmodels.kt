package de.mr_pine.recipes.android.models.instructions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.android.models.MutableStateSerializer
import de.mr_pine.recipes.android.models.RecipeIngredient
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
                when (this) {
                    is TimerModel -> EmbedTypeEnum.TIMER
                    is IngredientModel -> EmbedTypeEnum.INGREDIENT
                    else -> EmbedTypeEnum.UNDEFINED
                }
        }

        fun copy(): EmbedTypeModel
    }

    @Serializable
    @SerialName("Undefined")
    class UndefinedEmbedTypeModel : EmbedTypeModel {
        override val content = ""
        override fun copy(): UndefinedEmbedTypeModel =
            UndefinedEmbedTypeModel()
    }

    @Serializable
    @SerialName("Timer")
    //TODO(With Kotlin 1.7.20, this should be easier)
    class TimerModel(
        @Serializable(with = DurationStateSerializer::class)
        @SerialName("duration")
        private var durationState: MutableState<@Contextual Duration>
    ) : EmbedTypeModel {

        var duration by durationState

        override val content
            get() = duration.toString()

        fun call(title: String, context: Context) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
        }

        override fun copy() = TimerModel(mutableStateOf(duration))

    }


    object DurationStateSerializer : KSerializer<MutableState<Duration>> {
        private val durationSerializer = Duration.serializer()

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MutableDurationState", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MutableState<Duration> {
            return mutableStateOf(decoder.decodeSerializableValue(durationSerializer))
        }

        override fun serialize(encoder: Encoder, value: MutableState<Duration>) {
            encoder.encodeSerializableValue(durationSerializer, value.value)
        }
    }

    @Serializable
    @SerialName("Ingredient")
    class IngredientModel(
        @SerialName("ingredient_ID")
        val ingredientId: String,
        @Serializable(with = MutableStateSerializer::class)
        @SerialName("display")
        val displayNameState: MutableState<String?>,
        @Serializable(with = MutableStateSerializer::class)
        @SerialName("amount_fraction")
        val amountFractionState: MutableState<Float> = mutableStateOf(1f),
        @SerialName("no_amount")
        val noAmount: Boolean = false
    ) : EmbedTypeModel {

        constructor(
            ingredientId: String = "",
            displayName: String? = null,
            amountFraction: Float = 1f,
            noAmount: Boolean = false
        ) : this(ingredientId, mutableStateOf(displayName), mutableStateOf(amountFraction), noAmount)

        var displayName by displayNameState
        var amountFraction by amountFractionState

        @Transient
        var ingredient: RecipeIngredient? = null

        override val content: String
            get() = ingredient?.let { "${if (!noAmount) "${it.unitAmount.amount} ${it.unitAmount.unit.displayValue()} " else ""}${displayName ?: it.name}" }
                ?: "???"

        fun receiveIngredient(
            getIngredientFraction: ((String, Float) -> RecipeIngredient)?
        ) {
            ingredient = getIngredientFraction?.invoke(ingredientId, amountFraction)
        }

        override fun copy() = IngredientModel(
            ingredientId, displayName, amountFraction, noAmount
        )

        companion object {
            val NO_INGREDIENT = IngredientModel("", null)
        }

    }

}