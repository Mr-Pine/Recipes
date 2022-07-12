package de.mr_pine.recipes.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.mr_pine.recipes.R

class RecipeIngredients(override val serialized: String) : RecipeDeserializable {

    var ingredients: MutableList<RecipeIngredient> = mutableListOf()

    init {
        deserialize()
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeDeserializable {
        ingredients = serialized.extractFromList().map { RecipeIngredient(it) }.toMutableList()

        return this
    }

    fun getPartialIngredient(name: String, amount: IngredientAmount, unit: IngredientUnit) =
        ingredients.find { name == it.name }?.getPartial(amount, unit)
            ?: throw Exception("Ingredient not found")

    fun getPartialIngredient(name: String, fraction: Float) =
        ingredients.find { name == it.name }?.getPartial(fraction)
            ?: throw Exception("Ingredient $name not found")

    companion object {

        const val DataTag = "Ingredients"
    }
}

class RecipeIngredient(override val serialized: String) : RecipeDeserializable {
    var name: String = ""
    var amount: IngredientAmount = 0.amount
    var unit: IngredientUnit = IngredientUnit.None
    var isChecked by mutableStateOf(false)

    init {
        deserialize()
    }

    constructor(
        name: String,
        amount: IngredientAmount,
        unit: IngredientUnit
    ) : this("Name: \"$name\" Amount: \"$amount\" Unit: \"${unit.identifiers[0]}\"")

    fun getPartial(amount: IngredientAmount, unit: IngredientUnit): RecipeIngredient {
        return RecipeIngredient(name, amount, unit)
    }

    fun getPartial(fraction: Float): RecipeIngredient {
        return RecipeIngredient(name, amount * fraction, unit)
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeIngredient {
        name = serialized.extractString("Name")
        amount = serialized.extractString("Amount").replace(',', '.').toFloat().amount
        unit = IngredientUnit.values()
            .find { it.identifiers.contains(serialized.extractString("Unit")) }
            ?: throw Exception("unknown unit in $serialized: ${serialized.extractString("Unit")}")

        adjustUnit()

        return this
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

@JvmInline
value class IngredientAmount(val value: Float) : Comparable<IngredientAmount> {
    override fun toString(): String =
        if (value.toInt().toFloat() == value) value.toInt().toString() else String.format("%.2f", value)

    val roundToInt get() = IngredientAmount(value.toInt().toFloat())

    override fun compareTo(other: IngredientAmount): Int =
        value.compareTo(other.value)

    operator fun times(other: Float) = IngredientAmount(value.times(other))
    operator fun times(other: Int) = IngredientAmount(value.times(other))

    operator fun div(other: Float) = IngredientAmount(value.div(other))
    operator fun div(other: Int) = IngredientAmount(value.div(other))
}


enum class IngredientUnit(
    val type: UnitType,
    val identifiers: List<String>,
    val unitRelation: UnitRelation? = null
) {
    Gram(
        type = UnitType.Mass,
        identifiers = listOf("gramm", "g", "gram", "grams")
    ) {
        @Composable
        override fun displayValueLong(): String {
            return stringResource(R.string.grams)
        }

        override fun displayValue(): String {
            return "g"
        }
    },
    Kilogram(
        type = UnitType.Mass,
        identifiers = listOf("kilogramm", "kg", "kilogram", "kilograms", "kilo"),
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
    Milliliter(
        type = UnitType.Volume,
        identifiers = listOf("milliliter", "milliliters", "millilitre", "millilitres", "ml")
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
        type = UnitType.Volume,
        identifiers = listOf("liter", "liters", "litre", "litres", "l"),
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
    None(type = UnitType.None, identifiers = listOf("none")) {
        @Composable
        override fun displayValueLong(): String {
            return ""
        }

        override fun displayValue(): String {
            return ""
        }
    };

    abstract fun displayValue(): String

    @Composable
    abstract fun displayValueLong(): String
}

enum class UnitType {
    Volume,
    Mass,
    None
}

class UnitRelation(
    val conversionFactor: Float,
    val other: IngredientUnit
)