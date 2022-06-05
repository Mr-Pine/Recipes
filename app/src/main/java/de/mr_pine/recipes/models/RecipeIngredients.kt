package de.mr_pine.recipes.models

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R

class RecipeIngredients(override val serialized: String) : RecipeDeserializable {

    var ingredients: MutableList<RecipeIngredient> = mutableListOf()

    init {
        deserialize()
    }

    override fun deserialize(): RecipeDeserializable {
        ingredients = serialized.extractFromList().map { RecipeIngredient(it) }.toMutableList()

        return this
    }

    fun getPartialIngredient(name: String, amount: IngredientAmount, unit: Unit) =
        ingredients.find { name == it.name }?.getPartial(amount, unit)
            ?: throw Exception("Ingredient not found")

    fun getPartialIngredient(name: String, fraction: Float) =
        ingredients.find { name == it.name }?.getPartial(fraction)
            ?: throw Exception("Ingredient not found")

    companion object {

        const val DataTag = "Ingredients"
    }

    @ExperimentalMaterial3Api
    @Composable
    fun IngredientsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.Ingredients),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                )
                for (ingredient in ingredients) {
                    ingredient.IngredientRow()
                }
            }
        }
    }
}

class RecipeIngredient(override val serialized: String) : RecipeDeserializable {
    var name: String = ""
    var amount: IngredientAmount = 0.amount
    var unit: Unit = Unit.None

    init {
        deserialize()
    }

    constructor(
        name: String,
        amount: IngredientAmount,
        unit: Unit
    ) : this("Name: \"$name\" Amount: \"$amount\" Unit: \"${unit.displayValue()}\"")

    fun getPartial(amount: IngredientAmount, unit: Unit): RecipeIngredient {
        return RecipeIngredient(name, amount, unit)
    }

    fun getPartial(fraction: Float): RecipeIngredient {
        return RecipeIngredient(name, amount * fraction, unit)
    }

    override fun deserialize(): RecipeIngredient {
        name = serialized.extractString("Name")
        amount = serialized.extractString("Amount").replace(',', '.').toFloat().amount
        unit = Unit.values().find { it.identifiers.contains(serialized.extractString("Unit")) }
            ?: throw Exception("unknown unit: ${serialized.extractString("Unit")}")

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
            Unit.values().map { it.unitRelation }.find { it?.other == unit }
                ?.let { relation ->
                    if (amount > relation.conversionFactor.amount && (amount / relation.conversionFactor * 10).let {
                            it == it
                        }) {
                        amount /= relation.conversionFactor
                        unit = Unit.values().find { it.unitRelation == relation }!!
                    }
                }
        }
    }

    @ExperimentalMaterial3Api
    @Composable
    fun IngredientRow() {
        var checked by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .alpha(if (checked) 0.5f else 1f)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { checked = !checked }
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize)
            )
            Text(
                text = "$name: $amount ${unit.displayValue()}",
                fontSize = 20.sp
            )
        }
    }
}

inline val Float.amount: IngredientAmount get() = IngredientAmount(this)
inline val Int.amount: IngredientAmount get() = IngredientAmount(this.toFloat())

@JvmInline
value class IngredientAmount(val value: Float) : Comparable<IngredientAmount> {
    override fun toString(): String =
        if (value.toInt().toFloat() == value) value.toInt().toString() else value.toString()

    val roundToInt get() = IngredientAmount(value.toInt().toFloat())

    override fun compareTo(other: IngredientAmount): Int =
        value.compareTo(other.value)

    operator fun times(other: Float) = IngredientAmount(value.times(other))
    operator fun times(other: Int) = IngredientAmount(value.times(other))

    operator fun div(other: Float) = IngredientAmount(value.div(other))
    operator fun div(other: Int) = IngredientAmount(value.div(other))
}


enum class Unit(
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
    val other: Unit
)