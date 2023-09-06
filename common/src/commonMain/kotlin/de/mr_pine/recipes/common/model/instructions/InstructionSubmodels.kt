package de.mr_pine.recipes.common.model.instructions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import de.mr_pine.recipes.common.model.IngredientUnit
import de.mr_pine.recipes.common.model.MutableStateSerializer
import de.mr_pine.recipes.common.model.RecipeIngredient
import de.mr_pine.recipes.common.model.UnitAmount
import de.mr_pine.recipes.common.translation.ITranslation
import de.mr_pine.recipes.common.translation.Translation
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
        val modelName: ITranslation,
        val selectable: Boolean = true
    ) {

        UNDEFINED(Icons.Default.QuestionMark, Translation.undefined, false),
        TIMER(Icons.Default.Timer, Translation.timer),
        INGREDIENT(Icons.Default.Scale, Translation.ingredient);
    }

    interface EmbedTypeModel {
        fun content(ingredients: List<RecipeIngredient>? = null): String

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
        override fun content(ingredients: List<RecipeIngredient>?): String = ""
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

        override fun content(ingredients: List<RecipeIngredient>?): String = duration.toString()



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
        @Serializable(with = MutableStateSerializer::class)
        @SerialName("no_amount")
        val noAmountState: MutableState<Boolean> = mutableStateOf(false)
    ) : EmbedTypeModel {

        constructor(
            ingredientId: String = "",
            displayName: String? = null,
            amountFraction: Float = 1f,
            noAmount: Boolean = false
        ) : this(ingredientId, mutableStateOf(displayName), mutableStateOf(amountFraction), mutableStateOf(noAmount))

        var displayName by displayNameState
        var amountFraction by amountFractionState
        var noAmount by noAmountState
        var specifyUnit: IngredientUnit? by mutableStateOf(null)

        private fun UnitAmount.adjustToUnit(unit: IngredientUnit?) = adjustUnit().let {adjusted -> unit?.let { adjusted.asUnit(it) } ?: adjusted }
        override fun content(ingredients: List<RecipeIngredient>?) =
            ingredients?.find { it.ingredientId == ingredientId }?.let { "${if (!noAmount) "${(it.unitAmount * amountFraction).adjustToUnit(specifyUnit).amount} ${(it.unitAmount * amountFraction).adjustToUnit(specifyUnit).unit.displayValue()} " else ""}${displayName ?: it.name}" }
                ?: "???"

        override fun copy() = IngredientModel(
            ingredientId, displayName, amountFraction, noAmount
        )

        companion object {
            val NO_INGREDIENT = IngredientModel("", null)
        }

    }

}