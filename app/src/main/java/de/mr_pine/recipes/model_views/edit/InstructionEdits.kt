package de.mr_pine.recipes.model_views.edit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import de.mr_pine.recipes.R
import de.mr_pine.recipes.model_views.view.generateInlineContent
import de.mr_pine.recipes.models.*
import de.mr_pine.recipes.models.instructions.InstructionSubmodels.*
import de.mr_pine.recipes.models.instructions.InstructionSubmodels.EmbedTypeEnum.*
import de.mr_pine.recipes.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.models.instructions.RecipeInstruction
import de.mr_pine.recipes.models.instructions.encodeInstructionString
import java.lang.Integer.min
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.InstructionEditCard(
    index: Int,
    currentlyActiveIndex: Int,
    recipeTitle: String,
    setCurrentlyActiveIndex: (Int) -> Unit,
    setNextActive: () -> Unit,
    getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
    ingredients: List<RecipeIngredient>
) {

    val containerColor = MaterialTheme.colorScheme.let {
        if (done) it.surface.copy(alpha = 0.38f)
            .compositeOver(it.surfaceColorAtElevation(1.dp)) else it.surface
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColorFor(backgroundColor = containerColor).copy(alpha = if (done) 0.38f else 1f)
        )
    ) {

        var isEditingText by remember { mutableStateOf(false) }


        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val instructionTextStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 22.sp,
                lineHeight = 27.sp
            )
            if (isEditingText) {

                var bufferText by remember {
                    mutableStateOf(
                        encodeInstructionString(content)
                    )
                }

                Column {
                    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                        inlineEmbeds.forEachIndexed { index, embedData ->
                            embedData.RecipeEditChipStateful(
                                getIngredientFraction = getIngredientFraction,
                                done = done,
                                editIndex = index,
                                inlineEmbeds::remove,
                                ingredients
                            )
                        }
                        Row(modifier = Modifier.height(32.dp)) {
                            FilterChip(
                                onClick = {
                                    inlineEmbeds.add(
                                        RecipeInstruction.EmbedData(
                                            true,
                                            mutableStateOf(UndefinedEmbedTypeModel())
                                        )
                                    )
                                },
                                label = { Text(text = stringResource(id = R.string.Add)) },
                                selected = true,
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.12f
                                    ),
                                    selectedBorderWidth = 1.dp
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add"
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = bufferText,
                        onValueChange = {
                            bufferText = it
                        },
                        textStyle = instructionTextStyle
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { /*TODO*/ }) {
                            Text(text = stringResource(id = R.string.Apply))
                        }
                        TextButton(onClick = {
                            bufferText = encodeInstructionString(content); isEditingText = false
                        }) {
                            Text(stringResource(id = R.string.Cancel))
                        }
                    }
                }

            } else {
                Column(
                    modifier = Modifier
                        .width(0.dp)
                        .weight(1f)
                ) {
                    SubcomposeLayout { constraints ->

                        val inlineContent = inlineEmbeds.mapIndexed { index, embedData ->
                            val data =
                                generateInlineContent(
                                    index.toString(),
                                    constraints = constraints,
                                    content = {
                                        embedData.RecipeEditChipStateful(
                                            getIngredientFraction = getIngredientFraction,
                                            done = done,
                                            removeEmbed = inlineEmbeds::remove,
                                            ingredients = ingredients
                                        )
                                    })
                            index.toString() to data
                        }.toMap()

                        val contentPlaceable = subcompose("content") {

                            Text(
                                text = content,
                                inlineContent = inlineContent,
                                style = instructionTextStyle
                            )

                        }[0].measure(constraints)

                        layout(contentPlaceable.width, contentPlaceable.height) {
                            contentPlaceable.place(0, 0)
                        }
                    }
                }
                FilledTonalIconButton(
                    onClick = { isEditingText = !isEditingText }
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }

            }
        }
    }
}

private const val TAG = "InstructionEdits"

@ExperimentalMaterial3Api
@Composable
private fun RecipeInstruction.EmbedData.RecipeEditChipStateful(
    getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
    done: Boolean,
    editIndex: Int? = null,
    removeEmbed: (RecipeInstruction.EmbedData) -> Unit,
    ingredients: List<RecipeIngredient>
) {

    if (embed is IngredientModel && (embed as IngredientModel).ingredient == null) {
        (embed as IngredientModel).receiveIngredient(getIngredientFraction)
    }

    var hideNew by remember { mutableStateOf(embed is UndefinedEmbedTypeModel) }
    var isEditing by remember { mutableStateOf(hideNew) }

    if (!hideNew) {
        RecipeEditChip(
            onClick = { isEditing = true },
            selected = true,
            enabled = !done,
            icon = embed.getEnum().icon,
            labelText = embed.content,
            editIndex = editIndex
        )
    }

    if (isEditing) {
        val buffer by remember { mutableStateOf(this.copy(embedState = mutableStateOf(embed.copy()))) }
        val typeBuffers = remember {
            EmbedTypeEnum.values().map {
                it to when (it) {
                    TIMER -> TimerModel(mutableStateOf(0.seconds))
                    UNDEFINED -> UndefinedEmbedTypeModel()
                    INGREDIENT -> IngredientModel.NO_INGREDIENT
                }
            }.toMutableStateMap()
        }
        val ingredientBuffers = remember {
            mutableStateMapOf<RecipeIngredient, IngredientModel>()
            /*ingredients.map {
                it to IngredientModel(it.ingredientId)
            }.toMutableStateMap()*/
        }
        LaunchedEffect(key1 = ingredients) {
            ingredients.forEach { ingredient ->
                if (!ingredientBuffers.containsKey(ingredient)) ingredientBuffers[ingredient] =
                    IngredientModel(ingredient.ingredientId, null)
            }
        }

        typeBuffers[buffer.embed.getEnum()] = buffer.embed
        @Composable
        fun EditEmbedDialog(applyBufferConfirm: EmbedTypeModel.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
            val dismiss = { isEditing = false; if (hideNew) removeEmbed(this) }
            AlertDialog(
                onDismissRequest = dismiss,
                confirmButton = {
                    TextButton(onClick = {
                        if (
                            !(buffer.embed is TimerModel && (buffer.embed as TimerModel).duration == 0.seconds) &&
                            buffer.embed !is UndefinedEmbedTypeModel
                        ) {
                            embed = buffer.embed.copy().apply(applyBufferConfirm)
                            enabled = buffer.enabled
                            isEditing = false
                            hideNew = false
                        }
                    }) {
                        Text(text = stringResource(id = if (hideNew) R.string.Add else R.string.Apply))
                    }
                },
                dismissButton = {
                    TextButton(onClick = dismiss) {
                        Text(text = stringResource(id = R.string.Cancel))
                    }
                },
                title = {
                    Text(text = stringResource(id = if (hideNew) R.string.Add_Embed else R.string.Edit_Embed))
                },
                text = { Column(content = content) }
            )
        }

        @Composable
        fun TypeDropDown() {
            var modelTypeDropdownExpanded by remember { mutableStateOf(false) }
            var selectedType: EmbedTypeEnum? by remember {
                mutableStateOf(
                    buffer.embed.getEnum().takeIf { it.selectable }
                )
            }
            ExposedDropdownMenuBox(
                expanded = modelTypeDropdownExpanded,
                onExpandedChange = {
                    modelTypeDropdownExpanded = !modelTypeDropdownExpanded
                }) {
                TextField(
                    readOnly = true,
                    value = selectedType?.let { stringResource(id = it.modelNameId) } ?: "",
                    leadingIcon =
                    selectedType?.let {
                        {
                            Icon(
                                it.icon,
                                contentDescription = null
                            )
                        }
                    },
                    onValueChange = {},
                    label = { Text(stringResource(R.string.Type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelTypeDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = modelTypeDropdownExpanded,
                    onDismissRequest = { modelTypeDropdownExpanded = false }) {
                    values().filter { it.selectable }
                        .forEach { embedType ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = embedType.icon,
                                            contentDescription = ""
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = stringResource(id = embedType.modelNameId))
                                    }
                                },
                                onClick = {
                                    selectedType = embedType
                                    buffer.embed = typeBuffers[embedType]!!
                                    modelTypeDropdownExpanded = false
                                })
                        }
                }
            }
        }

        //Different Dialogs necessary because of https://issuetracker.google.com/issues/221643630
        when (remember(buffer.embed) { buffer.embed.getEnum() }) {
            TIMER -> {
                EditEmbedDialog {
                    TypeDropDown()
                    Spacer(modifier = Modifier.height(10.dp))
                    var test by remember(
                        try {
                            (embed as TimerModel).duration
                        } catch (e: Exception) {
                            embed
                        }
                    ) {
                        mutableStateOf((buffer.embed as TimerModel).duration.toComponents { hours, minutes, seconds, _ ->
                            (hours.toString().padStart(2, '0') + minutes.toString()
                                .padStart(2, '0') + seconds.toString().padStart(2, '0')).let {
                                TextFieldValue(it, TextRange(it.length))
                            }
                        })
                    }
                    TextField(
                        value = test,
                        onValueChange = { newValue ->
                            val newText = newValue.text.trimStart('0').padStart(6, '0')
                            try {
                                val hours = newText.substring(0, newText.length - 4).toInt().hours
                                val minutes =
                                    newText.substring(newText.length - 4, newText.length - 2)
                                        .toInt().minutes
                                val seconds = newText.substring(newText.length - 2).toInt().seconds
                                val duration = hours + minutes + seconds
                                (buffer.embed as TimerModel).duration =
                                    duration
                            } catch (e: NumberFormatException) {
                                (buffer.embed as TimerModel).duration =
                                    Duration.ZERO
                            }
                            test = test.copy(
                                newText,
                                if (!newValue.selection.collapsed) newValue.selection else TextRange(
                                    newText.length
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = stringResource(R.string.Duration)) },
                        visualTransformation = DurationVisualTransformation(),
                        isError = (buffer.embed as TimerModel).duration == 0.seconds
                    )
                }
            }
            INGREDIENT -> {
                var selectedIngredient: RecipeIngredient? by remember {
                    mutableStateOf(
                        ingredients.find { (buffer.embed as IngredientModel).ingredientId == it.ingredientId }
                    )
                }
                var unitAmountBuffer by remember(selectedIngredient) {
                    mutableStateOf(
                        selectedIngredient?.unitAmount?.copy(amount = selectedIngredient!!.unitAmount.amount * (buffer.embed as IngredientModel).amountFraction)
                            ?: UnitAmount.NaN
                    )
                }
                EditEmbedDialog({
                    selectedIngredient?.unitAmount?.let {
                        (this as IngredientModel).amountFraction = unitAmountBuffer / it
                    }
                }) {
                    TypeDropDown()

                    Spacer(modifier = Modifier.height(10.dp))

                    var ingredientDropdrownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = ingredientDropdrownExpanded,
                        onExpandedChange = {
                            ingredientDropdrownExpanded = !ingredientDropdrownExpanded
                        }) {
                        TextField(
                            readOnly = true,
                            value = selectedIngredient?.name ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.Ingredient)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientDropdrownExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = ingredientDropdrownExpanded,
                            onDismissRequest = { ingredientDropdrownExpanded = false }) {
                            ingredients.forEach { ingredient ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = ingredient.name)
                                    },
                                    onClick = {
                                        selectedIngredient = ingredient
                                        buffer.embed = ingredientBuffers[ingredient]!!
                                        ingredientDropdrownExpanded = false
                                    })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
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
                                } catch (e: NumberFormatException) {
                                    Log.w(
                                        TAG,
                                        "IngredientEditRow: Couldn't convert $newValue to IngredientAmount"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text(text = stringResource(R.string.Amount)) },
                            isError = unitAmountBuffer.amount.value.isNaN() || unitAmountBuffer.amount == 0.amount || selectedIngredient?.unitAmount?.let { it < unitAmountBuffer } ?: false,
                            enabled = selectedIngredient != null
                        )
                        var unitDropDownExtended by remember { mutableStateOf(false) }
                        Spacer(modifier = Modifier.width(10.dp))
                        ExposedDropdownMenuBox(
                            modifier = Modifier.weight(2f),
                            expanded = unitDropDownExtended,
                            onExpandedChange = {
                                if (selectedIngredient != null) unitDropDownExtended =
                                    !unitDropDownExtended
                            }) {
                            TextField(
                                readOnly = true,
                                value = unitAmountBuffer.takeIf { it != UnitAmount.NaN }?.unit?.displayValue()
                                    ?: "",
                                onValueChange = {},
                                label = { Text(stringResource(R.string.Unit)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropDownExtended) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                enabled = selectedIngredient != null
                            )
                            ExposedDropdownMenu(
                                expanded = unitDropDownExtended,
                                onDismissRequest = { unitDropDownExtended = false }) {
                                IngredientUnit.values()
                                    .filter { it.unitType == unitAmountBuffer.unit.unitType }
                                    .forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(text = unit.menuDisplayValue()) },
                                            onClick = {
                                                unitAmountBuffer.unit = unit
                                                unitDropDownExtended = false
                                            })
                                    }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = selectedIngredient?.let { unitAmountBuffer.asBaseUnit().amount / it.unitAmount.asBaseUnit().amount * 100 }
                            ?.let{
                                if(it.isNaN())
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
                                selectedIngredient?.let{
                                    unitAmountBuffer = it.unitAmount.asBaseUnit().apply { amount *= fraction }.adjustUnit()
                                }
                            } catch (_: NumberFormatException) {}
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = "${stringResource(R.string.Amount)} (%)") },
                        isError = selectedIngredient?.unitAmount?.let { it < unitAmountBuffer } ?: false,
                        enabled = selectedIngredient != null,
                        visualTransformation = PercentVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = (buffer.embed as IngredientModel).displayName ?: "",
                        placeholder = {
                            Text(
                                text = selectedIngredient?.name ?: ""
                            )
                        },
                        onValueChange = {
                            (buffer.embed as IngredientModel).displayName =
                                it.takeIf { it.isNotEmpty() }
                        },
                        enabled = selectedIngredient != null,
                        label = { Text(text = stringResource(R.string.Display_name)) }
                    )
                }
            }
            UNDEFINED -> {
                EditEmbedDialog {
                    TypeDropDown()
                }
            }
        }
    }
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
                    val range = 1 until min(buffer.lastIndex, 4) step 2
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

@ExperimentalMaterial3Api
@Composable
fun RecipeEditChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean,
    icon: ImageVector,
    labelText: String,
    editIndex: Int?
) {
    val colors = FilterChipDefaults.elevatedFilterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSelectedContainerColor = Color.Transparent
    )

    val elevation = if (selected) FilterChipDefaults.elevatedFilterChipElevation(
        defaultElevation = 3.dp,
        pressedElevation = 3.dp,
        focusedElevation = 3.dp,
        hoveredElevation = 6.dp,
        draggedElevation = 12.dp,
        disabledElevation = 0.dp
    ) else FilterChipDefaults.elevatedFilterChipElevation()

    Box(
        modifier = modifier
            .height(32.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .then(
                    if (editIndex != null)
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                MaterialTheme.shapes.small
                            )
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    else
                        Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                )
        ) {
            if (editIndex != null) {
                Text(
                    text = "{{${editIndex}}}",
                    modifier = Modifier.padding(horizontal = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
            ElevatedFilterChip(
                onClick = onClick,
                selected = selected,
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = { Text(text = labelText) },
                colors = colors,
                elevation = elevation,
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.Transparent,
                    selectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledSelectedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.12f
                    ),
                    selectedBorderWidth = 1.dp
                )
            )
        }
    }
}