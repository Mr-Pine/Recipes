package de.mr_pine.recipes.models

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.google.android.material.color.MaterialColors
import de.mr_pine.recipes.R
import de.mr_pine.recipes.components.swipeabe.Swipeable
import de.mr_pine.recipes.ui.theme.Extended

private const val TAG = "RecipeInstructions"

class RecipeInstructions(override val serialized: String) : RecipeDeserializable {

    var instructions = mutableListOf<RecipeInstruction>()
        private set

    var currentlyActiveIndex = mutableStateOf(0)

    init {
        deserialize()
    }

    override fun deserialize(): RecipeInstructions {
        instructions = serialized.extractFromList()
            .mapIndexed { index, serialized -> RecipeInstruction(serialized, index) }
            .toMutableList()

        return this
    }

    companion object {
        const val DataTag = "Instructions"
    }
}

class RecipeInstruction(override val serialized: String, val index: Int) : RecipeDeserializable {

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

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @ExperimentalMaterial3Api
    @Composable
    fun InstructionCard(
        currentlyActiveIndex: Int,
        setCurrentlyActiveIndex: (Int) -> kotlin.Unit,
        setNextActive: () -> kotlin.Unit
    ) {

        val active = remember(currentlyActiveIndex) { currentlyActiveIndex == index }

        val annotatedContent by remember {
            mutableStateOf(buildAnnotatedString {
                val elementList = content.split("(?<!\\\\)(([{][{])|([}][}]))".toRegex())
                val elementOffset = if(elementList[0] == "") 1 else 0
                val partList = elementList.filter { it != "" }.mapIndexed { index, s -> InstructionPart(content = s, type = if (index % 2 == elementOffset) InstructionPart.PartType.TEXT else InstructionPart.PartType.EMBED)}
                Log.d(TAG, "InstructionCard: $elementList")
                for(part in partList){
                    when (part.type) {
                        InstructionPart.PartType.TEXT -> {
                            append(part.content)
                        }
                        InstructionPart.PartType.EMBED -> {
                            val embedType = "@\\S+".toRegex().find(part.content)?.value ?: throw Exception("Missing @[EmbedType]")

                            appendInlineContent("embed", "embed type: $embedType")
                        }
                    }
                }
            })
        }

        val inlineDividerContent = mapOf(
            Pair(
                // This tells the [CoreText] to replace the placeholder string "[divider]" by
                // the composable given in the [InlineTextContent] object.
                "embed",
                InlineTextContent(
                    // Placeholder tells text layout the expected size and vertical alignment of
                    // children composable.
                    Placeholder(
                        width = 141.45.sp,
                        height = 1.em,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
                    )
                ) { test ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
                        Text(text = test, Modifier.background(Color.Red))
                    }
                }
            )
        )

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
                                CardDefaults.cardColors().containerColor(true).value.toArgb(),
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
            rightSwipedDone = {toggleDone(); Log.d(TAG, "InstructionCard: done: $done, index: $index, currentIndex: $currentlyActiveIndex")}
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

                Column {
                    val dpToSpFactor = with(LocalDensity.current) {
                        1.dp.toSp()
                    }

                    SubcomposeLayout { constraints ->
                        val textWidth = dpToSpFactor * subcompose("sampleText") {
                            Text("embed type: @test")
                        }[0].measure(Constraints()).width.toDp().value

                        val contentPlaceable = subcompose("content") {
                            Text(textWidth.toString())
                        }[0].measure(constraints)
                        layout(contentPlaceable.width, contentPlaceable.height) {
                            contentPlaceable.place(0, 0)
                        }
                    }
                    
                    
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(text = annotatedContent, inlineContent = inlineDividerContent)
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
                                .padding(8.dp)
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

