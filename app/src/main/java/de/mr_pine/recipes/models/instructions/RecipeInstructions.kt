package de.mr_pine.recipes.models.instructions

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.google.android.material.color.MaterialColors
import de.mr_pine.recipes.R
import de.mr_pine.recipes.components.swipeabe.Swipeable
import de.mr_pine.recipes.models.IngredientAmount
import de.mr_pine.recipes.models.RecipeDeserializable
import de.mr_pine.recipes.models.RecipeIngredient
import de.mr_pine.recipes.models.extractFromList
import de.mr_pine.recipes.models.instructions.InstructionSubmodels.*
import de.mr_pine.recipes.screens.ShowError
import de.mr_pine.recipes.ui.theme.Extended

private const val TAG = "RecipeInstructions"

class RecipeInstructions(override val serialized: String, private val recipeTitle: String) :
    RecipeDeserializable {

    var instructions = mutableListOf<RecipeInstruction>()
        private set

    var currentlyActiveIndex = mutableStateOf(0)

    init {
        deserialize()
    }

    override fun deserialize(): RecipeInstructions {
        instructions = serialized.extractFromList()
            .mapIndexed { index, serialized -> RecipeInstruction(serialized, index, recipeTitle) }
            .toMutableList()

        return this
    }

    companion object {
        const val DataTag = "Instructions"
    }
}

class RecipeInstruction(
    override val serialized: String,
    private val index: Int,
    private val recipeTitle: String
) : RecipeDeserializable, InstructionSubmodels {

    private var content: String = serialized

    var done by mutableStateOf(false)

    init {
        content = serialized.split("\n").joinToString("\n") { it.trim() }.trim()
    }

    override fun deserialize(): RecipeDeserializable {
        return this
    }

    data class EmbedData(var enabled: Boolean, var inlineContent: InlineTextContent? = null)

    var inlineEmbeds = mutableStateMapOf<String, EmbedData>()

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalMaterial3Api
    @Composable
    fun InstructionCard(
        currentlyActiveIndex: Int,
        setCurrentlyActiveIndex: (Int) -> Unit,
        setNextActive: () -> Unit,
        getIngredientAbsolute: ((String, IngredientAmount, de.mr_pine.recipes.models.Unit) -> RecipeIngredient)?,
        getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
    ) {

        val active = currentlyActiveIndex == index

        val annotatedContent by remember(content) {
            mutableStateOf(buildAnnotatedString {
                val elementList = content.split("(?<!\\\\)(([{][{])|([}][}]))".toRegex())
                val elementOffset = if (elementList[0] == "") 1 else 0
                val partList = elementList.filter { it != "" }.mapIndexed { index, s ->
                    InstructionPart(
                        content = s,
                        type = if (index % 2 == elementOffset) InstructionPart.PartType.TEXT else InstructionPart.PartType.EMBED
                    )
                }
                for (part in partList) {
                    when (part.type) {
                        InstructionPart.PartType.TEXT -> {
                            append(part.content)
                        }
                        InstructionPart.PartType.EMBED -> {
                            val embedTypeResult = "@\\S+".toRegex().find(part.content)
                                ?: throw Exception("Missing @[EmbedType]")

                            val embedType = embedTypeResult.value

                            val embedContent = part.content.removeRange(embedTypeResult.range)
                                .trim()
                            val inlineId = "$embedType \"$embedContent\""
                            appendInlineContent(inlineId, "embed type: $embedType")
                            inlineEmbeds[inlineId] = EmbedData(enabled = true)
                        }
                    }
                }
            })
        }

        fun toggleDone() {
            done = !done
            Log.d(
                TAG,
                "toggleDone: done: $done, index: $index, currentIndex: $currentlyActiveIndex"
            )
            if (active && done) {
                setNextActive()
            } else if (!done)
                setCurrentlyActiveIndex(index)
        }

        val currentColor = if (done) Extended.revertOrange else Extended.doneGreen


        Swipeable(
            swipeRightComposable = { _, relative ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(
                            MaterialColors.layer(
                                CardDefaults.cardColors()
                                    .containerColor(true).value.toArgb(),
                                currentColor.accentContainer.toArgb(),
                                relative
                            )
                        )
                    ), modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = if (done) Icons.Default.AddTask else Icons.Default.TaskAlt,
                            contentDescription = "Done Status",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp),
                            tint = if (done) Color.Red else currentColor.onAccentContainer
                        )
                    }
                }
            },
            rightSwipedDone = {
                toggleDone(); Log.d(
                TAG,
                "InstructionCard: done: $done, index: $index, currentIndex: $currentlyActiveIndex"
            )
            }
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardDefaults.cardColors().containerColor(!done).value,
                    contentColor = CardDefaults.cardColors().contentColor(!done).value
                ),
                onClick = {
                    setCurrentlyActiveIndex(index)
                }
            ) {

                Column(modifier = Modifier.padding(12.dp)) {
                    SubcomposeLayout { constraints ->

                        val inlineContent = inlineEmbeds.mapValues {
                            val key = it.key
                            var data = it.value.inlineContent
                            if(data == null) {
                                data = generateInlineContent(key, constraints = constraints) {
                                    val contentResult = "(?<!\\\\)\".*(?<!\\\\)\"".toRegex().find(key)
                                        ?: throw Exception("Badly formatted inline Id: $it")
                                    val content =
                                        contentResult.value.substring(1, contentResult.value.length - 1)
                                    val parts = key.substring(0, contentResult.range.first).split(" ")
                                    val embedType = try {
                                        EmbedType[parts[0].substring(1)]
                                    } catch (e: Exception) {
                                        ShowError(e); EmbedType.UNKNOWN
                                    }
                                    val model = embedType.constructor?.invoke(content)

                                    when (model) {
                                        is IngredientModel -> model.receiveIngredient(
                                            getIngredientFraction,
                                            getIngredientAbsolute
                                        )
                                    }
                                    val defaultChipColor = AssistChipDefaults.elevatedAssistChipColors()
                                    val defaultChipColorDisabled =
                                        AssistChipDefaults.elevatedAssistChipColors(
                                            containerColor = defaultChipColor.containerColor(false).value,
                                            labelColor = defaultChipColor.labelColor(false).value,
                                            leadingIconContentColor = defaultChipColor.leadingIconContentColor(
                                                false
                                            ).value,
                                            trailingIconContentColor = defaultChipColor.trailingIconContentColor(
                                                false
                                            ).value
                                        )

                                    fun getChipColor(enabledColor: Boolean) =
                                        if (enabledColor) defaultChipColor else defaultChipColorDisabled

                                    val context = LocalContext.current

                                    var enabled by remember(inlineEmbeds[key]?.enabled) { mutableStateOf(inlineEmbeds[key]?.enabled ?: true) }
                                    fun setEnabled(value: Boolean) {inlineEmbeds[key]?.enabled = value; enabled = value}

                                    ElevatedAssistChip(
                                        onClick = {
                                            when (embedType) {
                                                EmbedType.INGREDIENT -> setEnabled(!enabled)
                                                EmbedType.TIMER -> (model as TimerModel).call(
                                                    recipeTitle,
                                                    context
                                                )
                                                else -> {}
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        colors = getChipColor(enabled),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (embedType) {
                                                    EmbedType.INGREDIENT -> Icons.Default.Scale
                                                    EmbedType.TIMER -> Icons.Default.Timer
                                                    EmbedType.UNKNOWN -> Icons.Default.QuestionMark
                                                },
                                                contentDescription = embedType.toString(),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        label = { Text(text = model?.content ?: key) },
                                        elevation = if (enabled) null else AssistChipDefaults.elevatedAssistChipElevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 0.dp
                                        ),
                                        enabled = !done
                                    )
                                }
                            }
                            data
                        }

                        val contentPlaceable = subcompose("content") {

                            Text(
                                text = annotatedContent,
                                inlineContent = inlineContent)


                        }[0].measure(constraints)

                        layout(contentPlaceable.width, contentPlaceable.height) {
                            contentPlaceable.place(0, 0)
                        }
                    }
                    AnimatedVisibility(
                        visible = active,
                        enter = scaleIn(initialScale = 0f) + expandVertically(expandFrom = Alignment.Top),
                        exit = scaleOut(targetScale = 0f) + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = ::toggleDone,
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = currentColor.onAccentContainer,
                                    containerColor = currentColor.accentContainer
                                )
                            ) {
                                Text(
                                    text = if (done) stringResource(R.string.step_repeat) else stringResource(
                                        R.string.step_finish
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun SubcomposeMeasureScope.generateInlineContent(
    id: String,
    constraints: Constraints = Constraints(),
    content: @Composable () -> Unit
): InlineTextContent {
    val (inlineWidth, inlineHeight) = subcompose(id, content)[0].measure(constraints)
        .let { Pair(it.width.toSp(), it.height.toSp()) }

    return InlineTextContent(
        Placeholder(
            width = inlineWidth,
            height = inlineHeight,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
        )
    ) {
        content()
    }
}