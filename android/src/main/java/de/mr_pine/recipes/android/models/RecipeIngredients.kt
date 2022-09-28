package de.mr_pine.recipes.android.models

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import de.mr_pine.recipes.android.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import kotlin.math.roundToInt

@Serializable
data class RecipeIngredients(
    @Serializable(with = MutableStateListSerializer::class)
    var ingredients: SnapshotStateList<RecipeIngredient>
) {
    fun getPartialIngredient(id: String, fraction: Float) =
        ingredients.find { id == it.ingredientId }?.getPartial(fraction)
            ?: throw Exception("Ingredient $id not found")
}

@Serializable
class RecipeIngredient(
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("name")
    private val nameState: MutableState<String> = mutableStateOf(""),
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("unit_amount")
    private val unitAmountState: MutableState<UnitAmount> = mutableStateOf(UnitAmount.NaN),
    @SerialName("ID")
    val ingredientId: String = UUID.randomUUID().toString()
) {
    var name by nameState

    var unitAmount by unitAmountState

    var isChecked by mutableStateOf(false)

    fun getPartial(fraction: Float): RecipeIngredient {
        return RecipeIngredient(
            mutableStateOf(name),
            mutableStateOf(unitAmount.copy(amount = unitAmount.amount * fraction))
        )
    }

    fun copyFrom(from: RecipeIngredient) {
        this.name = from.name
        this.unitAmount = from.unitAmount.copy()
    }

    fun copy(): RecipeIngredient {
        val temp = RecipeIngredient()
        temp.copyFrom(this)
        return temp
    }

    init {
        unitAmount = unitAmount.adjustUnit()
    }


}

@Serializable
class UnitAmount(
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("amount")
    private val amountState: MutableState<IngredientAmount>,
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("unit")
    private val unitState: MutableState<IngredientUnit> = mutableStateOf(IngredientUnit.None)
) : Comparable<UnitAmount> {

    constructor(amount: IngredientAmount, unit: IngredientUnit) : this(
        mutableStateOf(amount),
        mutableStateOf(unit)
    )

    var amount by amountState
    var unit by unitState

    fun adjustUnit(): UnitAmount {
        val newUnit = IngredientUnit.values().filter {
            it.unitType == unit.unitType && (!unit.unitType.enforceBiggerOne || it.baseFactor < unit.asBaseUnit(
                amount
            ).value)
        }.takeIf { it.isNotEmpty() }?.minBy { unit.asBaseUnit(amount) / it.baseFactor } ?: unit
        return UnitAmount(unit.asBaseUnit(amount) / newUnit.baseFactor, newUnit)
    }

    companion object {
        val NaN = UnitAmount(Float.NaN.amount, IngredientUnit.None)
    }

    fun copy(
        amount: IngredientAmount = this.amount.value.amount,
        unit: IngredientUnit = this.unit
    ) =
        UnitAmount(amount, unit)

    fun asBaseUnit() =
        UnitAmount(unit.asBaseUnit(amount), unit.unitType.baseUnit)

    override fun compareTo(other: UnitAmount) =
        asBaseUnit().amount.compareTo(other.asBaseUnit().amount)

    operator fun div(other: UnitAmount): Float = asBaseUnit().amount.div(other.asBaseUnit().amount)
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
        if (value.isNaN()) ""
        else if (String.format("%.2f", value.roundToInt().toFloat()) == String.format(
                "%.2f",
                value
            )
        )
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
    operator fun div(other: IngredientAmount) = value.div(other.value)

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
    val unitType: UnitType,
    val baseFactor: Float
) {
    Gram(UnitType.MASS, 0.001f) {
        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.grams)
        }

        override fun displayValue(): String {
            return "g"
        }
    },
    Kilogram(
        UnitType.MASS,
        1f
    ) {
        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.kilograms)
        }

        override fun displayValue(): String {
            return "kg"
        }
    },
    Milliliter(
        UnitType.VOLUME,
        0.001f
    ) {
        override fun displayValue(): String {
            return "ml"
        }

        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.milliliters)
        }

    },
    Liter(
        UnitType.VOLUME,
        1f
    ) {
        override fun displayValue(): String {
            return "l"
        }

        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.liters)
        }

    },
    None(UnitType.NONE, 1f) {
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

    fun asBaseUnit(amount: IngredientAmount) = amount * baseFactor
}

enum class UnitType(val enforceBiggerOne: Boolean) {
    NONE(false) {
        override val baseUnit: IngredientUnit
            get() = IngredientUnit.None
    },
    VOLUME(true) {
        override val baseUnit: IngredientUnit
            get() = IngredientUnit.Liter
    },
    MASS(true) {
        override val baseUnit: IngredientUnit
            get() = IngredientUnit.Kilogram
    };

    abstract val baseUnit: IngredientUnit
}