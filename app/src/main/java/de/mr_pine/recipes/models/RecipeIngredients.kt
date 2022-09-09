package de.mr_pine.recipes.models

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import de.mr_pine.recipes.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.roundToInt

@Serializable
data class RecipeIngredients(
    @Serializable(with = MutableStateListSerializer::class)
    var ingredients: SnapshotStateList<RecipeIngredient>
) {
    fun reorderIngredients(from: Int, to: Int) {
        ingredients.apply { add(to, removeAt(from)) }
    }

    fun getPartialIngredient(name: String, fraction: Float) =
        ingredients.find { name == it.name }?.getPartial(fraction)
            ?: throw Exception("Ingredient $name not found")
}

@Serializable
class RecipeIngredient(
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("name")
    private val nameState: MutableState<String> = mutableStateOf(""),
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("amount")
    private val amountState: MutableState<IngredientAmount> = mutableStateOf(Float.NaN.amount),
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("unit")
    private val unitState: MutableState<IngredientUnit> = mutableStateOf(IngredientUnit.None)
) {
    var name by nameState
    var amount by amountState
    var unit by unitState

    var isChecked by mutableStateOf(false)

    fun getPartial(fraction: Float): RecipeIngredient {
        return RecipeIngredient(mutableStateOf(name), mutableStateOf(amount * fraction), mutableStateOf(unit))
    }

    fun copyFrom(from: RecipeIngredient) {
        this.name = from.name
        this.amount = from.amount
        this.unit = from.unit
    }

    fun copy(): RecipeIngredient {
        val temp = RecipeIngredient()
        temp.copyFrom(this)
        return temp
    }

    init {
        adjustUnit()
    }

    private fun adjustUnit() {
        if (amount < 1.amount) {
            val relation = unit.unitRelation
            if (relation != null) {
                amount *= relation.conversionFactor
                unit = relation.other
            }
        } else {
            IngredientUnit.values().map { it.unitRelation }.find { it?.other == unit }
                ?.let { relation ->
                    if (amount > relation.conversionFactor.amount && (amount / relation.conversionFactor * 10).let {
                            it == it
                        }) {
                        amount /= relation.conversionFactor
                        unit = IngredientUnit.values().find { it.unitRelation == relation }!!
                    }
                }
        }
    }
}


inline val Float.amount: IngredientAmount get() = IngredientAmount(this)
inline val Int.amount: IngredientAmount get() = IngredientAmount(this.toFloat())
fun String.toAmount(): IngredientAmount {
    val thisVal = this.replace(',', '.')
    return try {
        this.apply {
            if (try {
                    thisVal.last() == '.'
                } catch (e: Exception) {
                    false
                }
            ) {
                subSequence(0 until this.lastIndex)
            }
        }.toInt().amount
    } catch (e: NumberFormatException) {
        thisVal.toFloat().amount
    }
}

@JvmInline
@Serializable(with = AmountSerializer::class)
value class IngredientAmount(val value: Float) : Comparable<IngredientAmount> {
    override fun toString(): String =
        if(value.isNaN()) ""
        else if (String.format("%.2f", value.roundToInt().toFloat()) == String.format("%.2f", value))
            value.roundToInt().toString()
        else
            String.format("%.2f", value)

    val roundToInt get() = IngredientAmount(value.toInt().toFloat())

    override fun compareTo(other: IngredientAmount): Int =
        value.compareTo(other.value)

    operator fun times(other: Float) = IngredientAmount(value.times(other))
    operator fun times(other: Int) = IngredientAmount(value.times(other))

    operator fun div(other: Float) = IngredientAmount(value.div(other))
    operator fun div(other: Int) = IngredientAmount(value.div(other))


}

object AmountSerializer : KSerializer<IngredientAmount> {
    override fun deserialize(decoder: Decoder): IngredientAmount {
        return decoder.decodeFloat().amount
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Amount", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: IngredientAmount) {
        encoder.encodeFloat(value.value)
    }
}

enum class IngredientUnit(
    val unitRelation: UnitRelation? = null
) {
    Gram {
        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.grams)
        }

        override fun displayValue(): String {
            return "g"
        }
    },
    Kilogram(
        unitRelation = UnitRelation(1000f, Gram)
    ) {
        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.kilograms)
        }

        override fun displayValue(): String {
            return "kg"
        }
    },
    Milliliter {
        override fun displayValue(): String {
            return "ml"
        }

        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.milliliters)
        }

    },
    Liter(
        unitRelation = UnitRelation(1000f, Milliliter)
    ) {
        override fun displayValue(): String {
            return "l"
        }

        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.liters)
        }

    },
    None {
        @Composable
        override fun displayValueLong(): String {
            return ""
        }

        @Composable
        override fun menuDisplayValue(): String {
            return stringResource(R.string.No_unit)
        }

        override fun displayValue(): String {
            return ""
        }
    };

    abstract fun displayValue(): String

    @Composable
    abstract fun displayValueLong(): String

    @Composable
    open fun menuDisplayValue() = displayValueLong()
}

class UnitRelation(
    val conversionFactor: Float,
    val other: IngredientUnit
)