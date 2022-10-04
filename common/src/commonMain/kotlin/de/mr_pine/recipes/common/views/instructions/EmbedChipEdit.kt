package de.mr_pine.recipes.common.views.instructions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.*
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.translation.Translation
import de.mr_pine.recipes.common.views.DropDown
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun RecipeInstruction.EmbedData.TypeDropDown(modifier: Modifier = Modifier, onSelect: (InstructionSubmodels.EmbedTypeEnum) -> Unit) {
    var modelTypeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedType: InstructionSubmodels.EmbedTypeEnum? by remember {
        mutableStateOf(
            embed.getEnum().takeIf { it.selectable }
        )
    }
    DropDown(
        modifier = modifier.fillMaxWidth(),
        expanded = modelTypeDropdownExpanded,
        expandOnFocus = selectedType == null,
        labelString = Translation.type.getString(),
        onDismissRequest = { modelTypeDropdownExpanded = false },
        onExpandedChange = { modelTypeDropdownExpanded = it },
        selectedString = selectedType?.modelName?.getString() ?: "",
        selectedIcon = selectedType?.icon,
        options = InstructionSubmodels.EmbedTypeEnum.values().filter { it.selectable },
        optionText = { it.modelName.getString() },
        optionIcon = { it.icon }
    ) { embedTypeEnum ->
        selectedType = embedTypeEnum
        onSelect(embedTypeEnum)
        true
    }
}

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.EmbedData.TimerEditColumn(
    timerEmbed: InstructionSubmodels.TimerModel,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onTypeSelect: (InstructionSubmodels.EmbedTypeEnum) -> Unit
) {
    TypeDropDown(onSelect = onTypeSelect)
    Spacer(modifier = Modifier.height(10.dp))
    var duration by remember(
        try {
            (embed as InstructionSubmodels.TimerModel).duration
        } catch (e: Exception) {
            embed
        }
    ) {
        mutableStateOf(timerEmbed.duration.toComponents { hours, minutes, seconds, _ ->
            (hours.toString().padStart(2, '0') + minutes.toString()
                .padStart(2, '0') + seconds.toString().padStart(2, '0')).let {
                TextFieldValue(it, TextRange(it.length))
            }
        })
    }
    TextField(
        value = duration,
        onValueChange = { newValue ->
            val newText = newValue.text.trimStart('0').padStart(6, '0')
            try {
                val hours = newText.substring(0, newText.length - 4).toInt().hours
                val minutes =
                    newText.substring(newText.length - 4, newText.length - 2)
                        .toInt().minutes
                val seconds = newText.substring(newText.length - 2).toInt().seconds
                val newDuration = hours + minutes + seconds
                timerEmbed.duration =
                    newDuration
            } catch (e: NumberFormatException) {
                timerEmbed.duration =
                    Duration.ZERO
            }
            duration = duration.copy(
                newText,
                if (!newValue.selection.collapsed) newValue.selection else TextRange(
                    newText.length
                )
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        label = { Text(text = Translation.duration.getString()) },
        visualTransformation = DurationVisualTransformation(),
        isError = timerEmbed.duration == 0.seconds,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine = true
    )
}

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.EmbedData.IngredientEditColumn(
    ingredientEmbed: InstructionSubmodels.IngredientModel,
    ingredients: List<RecipeIngredient>,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onTypeSelect: (InstructionSubmodels.EmbedTypeEnum) -> Unit
) {

    var selectedIngredient: RecipeIngredient? by remember {
        mutableStateOf(
            ingredients.find { ingredientEmbed.ingredientId == it.ingredientId }
        )
    }
    var unitAmountBuffer by remember(selectedIngredient) {
        mutableStateOf(
            selectedIngredient?.unitAmount?.copy(amount = selectedIngredient!!.unitAmount.amount * ingredientEmbed.amountFraction)
                ?: UnitAmount.NaN
        )
    }

    LaunchedEffect(unitAmountBuffer == selectedIngredient?.unitAmount) {
        ingredientEmbed.noAmount = unitAmountBuffer == selectedIngredient?.unitAmount
    }

    TypeDropDown(onSelect = onTypeSelect)

    Spacer(modifier = Modifier.height(10.dp))

    val ingredientBuffers = remember {
        mutableStateMapOf<RecipeIngredient, InstructionSubmodels.IngredientModel>()
    }
    LaunchedEffect(key1 = ingredients) {
        ingredients.forEach { ingredient ->
            if (!ingredientBuffers.containsKey(ingredient)) ingredientBuffers[ingredient] =
                InstructionSubmodels.IngredientModel(ingredient.ingredientId, null)
        }
    }

    var ingredientDropdownExpanded by remember { mutableStateOf(false) }
    DropDown(
        expanded = ingredientDropdownExpanded,
        expandOnFocus = selectedIngredient == null,
        onExpandedChange = { ingredientDropdownExpanded = it },
        onDismissRequest = { ingredientDropdownExpanded = false },
        selectedString = selectedIngredient?.name ?: "",
        labelString = Translation.ingredient.getString(),
        options = ingredients,
        optionText = { it.name },
        optionClick = {
            selectedIngredient?.let { oldIngredient -> ingredientBuffers[oldIngredient] = ingredientEmbed }
            selectedIngredient = it
            embed = ingredientBuffers[it]!!
            true
        },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
    )

    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = unitAmountBuffer.amount.takeIf { !it.value.isNaN() }?.toString()
                ?: "",
            modifier = Modifier.weight(1f),
            onValueChange = { newValue ->
                if (newValue == "") {
                    unitAmountBuffer.amount = Float.NaN.amount
                }
                try {
                    unitAmountBuffer.amount = newValue.toAmount()
                } catch (_: NumberFormatException) {
                }
                selectedIngredient?.unitAmount?.let {
                    ingredientEmbed.amountFraction = unitAmountBuffer / it
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text(text = Translation.amount.getString()) },
            isError = unitAmountBuffer.amount.value.isNaN() || unitAmountBuffer.amount == 0.amount || selectedIngredient?.unitAmount?.let { it < unitAmountBuffer } ?: false,
            enabled = selectedIngredient != null,
            singleLine = true
        )

        Spacer(modifier = Modifier.width(10.dp))

        var unitDropDownExtended by remember { mutableStateOf(false) }
        DropDown(
            modifier = Modifier.width(IntrinsicSize.Max).weight(2f),
            expanded = unitDropDownExtended,
            onExpandedChange = {
                if (selectedIngredient != null) unitDropDownExtended =
                    !unitDropDownExtended
            },
            onDismissRequest = { unitDropDownExtended = false },
            labelString = Translation.unit.getString(),
            selectedString = unitAmountBuffer.takeIf { it != UnitAmount.NaN }?.unit?.displayValue() ?: "",
            options = IngredientUnit.values().filter { it.unitType == unitAmountBuffer.unit.unitType },
            optionText = { it.menuDisplayValue() },
            optionClick = {
                unitAmountBuffer.unit = it
                true
            }
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = selectedIngredient?.let { unitAmountBuffer.asBaseUnit().amount / it.unitAmount.asBaseUnit().amount * 100 }
                ?.let {
                    if (it.isNaN())
                        "0"
                    else if (String.format("%.2f", it.roundToInt().toFloat()) == String.format(
                            "%.2f",
                            it
                        )
                    )
                        it.roundToInt().toString()
                    else
                        String.format("%.2f", it)
                } ?: "",
            onValueChange = { newPercentage ->
                try {
                    val fraction = newPercentage.replace(',', '.').padStart(1, '0').toFloat() * 0.01f
                    selectedIngredient?.let {
                        unitAmountBuffer =
                            it.unitAmount.asBaseUnit().apply { amount *= fraction }.adjustUnit()
                    }
                } catch (_: NumberFormatException) {
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text(text = "${Translation.amount.getString()} (%)") },
            isError = selectedIngredient?.unitAmount?.let { it < unitAmountBuffer } ?: false,
            enabled = selectedIngredient != null,
            visualTransformation = PercentVisualTransformation(),
            modifier = Modifier.width(0.dp).weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Show amount")
        Spacer(modifier = Modifier.width(10.dp))
        Switch(checked = !ingredientEmbed.noAmount, onCheckedChange = {ingredientEmbed.noAmount = !ingredientEmbed.noAmount})
    }
    Spacer(modifier = Modifier.height(10.dp))
    TextField(
        value = ingredientEmbed.displayName ?: ingredientEmbed.ingredient?.name ?: "",
        placeholder = {
            Text(
                text = selectedIngredient?.name ?: ""
            )
        },
        onValueChange = {
            ingredientEmbed.displayName =
                it.takeIf { it.isNotEmpty() }
        },
        enabled = selectedIngredient != null,
        label = { Text(text = Translation.displayName.getString()) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

class PercentVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(text = text.text + " %"),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = offset

                override fun transformedToOriginal(offset: Int) = offset.coerceAtMost(text.length)

            }
        )
    }

}

class DurationVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(text.text.let {
                var buffer = it.reversed()
                if (buffer.isNotEmpty()) {
                    val range = 1 until Integer.min(buffer.lastIndex, 4) step 2
                    for (i in range) {
                        val transformedIndex = (i * 1.5).toInt()
                        buffer =
                            buffer.replaceRange(
                                transformedIndex..transformedIndex,
                                buffer[transformedIndex] + ":"
                            )
                    }
                }
                buffer.reversed()
            }),
            offsetMapping = object :
                OffsetMapping { //    0:12:34 01234  //365 //0123456 //0:12:34:56| -> |65:43:21:0
                override fun originalToTransformed(offset: Int): Int {
                    val transformedLength =
                        ((text.length * 1.5).toInt() - (1 - text.length % 2)).coerceAtMost(text.length + 2)
                    val reversed = text.length - offset
                    val new = reversed + (reversed / 2)
                    val rereversed = transformedLength - new
                    return rereversed.coerceAtLeast(0)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val transformedLength =
                        ((text.length * 1.5).toInt() - (1 - text.length % 2)).coerceAtMost(text.length + 2)
                    val reversed = transformedLength - offset
                    val new = reversed - (reversed / 3)
                    val rereversed = text.length - new
                    return rereversed.coerceAtLeast(0)
                }

            }
        )
    }
}