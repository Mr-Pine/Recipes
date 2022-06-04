package de.mr_pine.recipes.models

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
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
import de.mr_pine.recipes.screens.ShowError
import de.mr_pine.recipes.ui.theme.Extended
import kotlin.Unit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    val index: Int,
    private val recipeTitle: String
) : RecipeDeserializable {

    var content: String = serialized

    var done by mutableStateOf(false)

    init {
        content = serialized.split("\n").joinToString("\n") { it.trim() }.trim()
    }

    override fun deserialize(): RecipeDeserializable {
        return this
    }

    private data class InstructionPart(val content: String, val type: PartType) {
        enum class PartType(identifier: Int) {
            TEXT(0), EMBED(1)
        }
    }

    private interface PartTypeModel {
        var content: String
    }

    class TimerModel(val duration: Duration, override var content: String) : PartTypeModel {

        fun call(title: String, context: Context) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_LENGTH, duration.inWholeSeconds.toInt())
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
            context.startActivity(intent)
        }

        companion object {
            fun fromString(string: String): TimerModel {
                val duration = string.toInt().seconds
                return TimerModel(duration, duration.toString())
            }
        }
    }

    private enum class PartType(val getModel: ((String) -> PartTypeModel)?) {
        INGREDIENT(null), TIMER((TimerModel)::fromString), UNKNOWN(null);

        companion object {
            operator fun get(type: String): PartType {
                return when (type.trim().lowercase()) {
                    "ingredient" -> INGREDIENT
                    "timer" -> TIMER
                    else -> throw Exception("Bad type: $type")
                }
            }
        }
    }


    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalMaterial3Api
    @Composable
    fun InstructionCard(
        currentlyActiveIndex: Int,
        setCurrentlyActiveIndex: (Int) -> kotlin.Unit,
        setNextActive: () -> kotlin.Unit
    ) {

        val active = remember(currentlyActiveIndex) { currentlyActiveIndex == index }

        val inlineIds = remember(content) { mutableStateListOf<String>() }

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
                Log.d(TAG, "InstructionCard: $elementList")
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
                                .trim() //TODO: Add proper content
                            val inlineId = "$embedType \"$embedContent\""
                            appendInlineContent(inlineId, "embed type: $embedType")
                            inlineIds.add(inlineId)
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
                    setCurrentlyActiveIndex(index); Log.d(
                    TAG,
                    "InstructionCard: $currentlyActiveIndex"
                )
                }
            ) {

                Column(modifier = Modifier.padding(12.dp)) {
                    SubcomposeLayout { constraints ->


                        val inlineDividerContent = inlineIds.associateWith {
                            generateInlineContent(it, constraints = constraints) {
                                val contentResult = "(?<!\\\\)\".*(?<!\\\\)\"".toRegex().find(it)
                                    ?: throw Exception("Badly formatted inline Id: $it")
                                val content =
                                    contentResult.value.substring(1, contentResult.value.length - 1)
                                val parts = it.substring(0, contentResult.range.first).split(" ")
                                val embedType = try {
                                    PartType[parts[0].substring(1)]
                                } catch (e: Exception) {
                                    ShowError(errorMessage = e.message ?: ""); PartType.UNKNOWN
                                }
                                val model = embedType.getModel?.invoke(content)

                                var enabled by remember { mutableStateOf(true) }
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

                                ElevatedAssistChip(
                                    onClick = {
                                        when (embedType) {
                                            PartType.INGREDIENT -> enabled = !enabled
                                            PartType.TIMER -> (model as TimerModel).call(
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
                                                PartType.INGREDIENT -> Icons.Default.Scale
                                                PartType.TIMER -> Icons.Default.Timer
                                                PartType.UNKNOWN -> Icons.Default.QuestionMark
                                            },
                                            contentDescription = embedType.toString(),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    label = { Text(text = model?.content ?: it) },
                                    elevation = if (enabled) null else AssistChipDefaults.elevatedAssistChipElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                    )
                                )
                            }
                        }

                        val contentPlaceable = subcompose("content") {

                            Text(text = annotatedContent, inlineContent = inlineDividerContent)


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
