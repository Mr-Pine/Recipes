package de.mr_pine.recipes.models

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorConfig
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.components.swipeabe.Swipeable
import kotlin.math.max

class RecipeInstructions(override val serialized: String) : RecipeDeserializable {

    var instructions = mutableListOf<RecipeInstruction>()
        private set

    init {
        deserialize()
    }

    override fun deserialize(): RecipeInstructions {
        instructions = serialized.extractFromList().map { RecipeInstruction(it) }.toMutableList()

        return this
    }

    companion object {
        const val DataTag = "Instructions"
    }
}

class RecipeInstruction(
    override val serialized: String

) : RecipeDeserializable {
    override fun deserialize(): RecipeDeserializable {
        return this
    }


    @ExperimentalMaterialApi
    @ExperimentalMaterial3Api
    @Composable
    fun InstructionCard() {
        Swipeable(
            swipeRightComposable = { _, relativeRaw ->
                val relative = max(relativeRaw * 3f - 2f, 0f)
                Card(colors = CardDefaults.cardColors(), modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {

                        val image = Icons.Outlined.TaskAlt

                        class StrokeConfig(
                            private val lineWidth: Float,
                            private val stroke: Brush?
                        ) : VectorConfig {

                            override fun <T> getOrDefault(
                                property: VectorProperty<T>,
                                defaultValue: T
                            ): T {
                                return when (property) {
                                    is VectorProperty.StrokeLineWidth -> lineWidth as T
                                    is VectorProperty.Stroke -> stroke as T
                                    else -> super.getOrDefault(property, defaultValue)
                                }
                            }
                        }

                        val donePainter = rememberVectorPainter(
                            defaultWidth = image.defaultWidth,
                            defaultHeight = image.defaultHeight,
                            viewportWidth = image.viewportWidth,
                            viewportHeight = image.viewportHeight,
                            name = image.name,
                            tintColor = image.tintColor,
                            tintBlendMode = image.tintBlendMode,
                            autoMirror = image.autoMirror,
                            content = { _, _ ->
                                RenderVectorGroup(
                                    group = image.root, configs = mapOf(
                                        image.root.name to StrokeConfig(
                                            1f, SolidColor(
                                                Color.Black
                                            )
                                        )
                                    )
                                )
                            }
                        )


                        Canvas(modifier = Modifier.padding(start = 16.dp), onDraw = {
                            val iconSize = donePainter.intrinsicSize * 1.5f
                            val outlineFactor = 1.5f
                            translate(
                                left = iconSize.width * (outlineFactor * 0.5f - 0.5f),
                                top = iconSize.height * -0.5f
                            ) {
                                with(donePainter) {
                                    draw(iconSize, colorFilter = ColorFilter.tint(Color.Green))
                                    /*drawArc(
                                        Color.Green,
                                        135 * relative,
                                        360 * 1.5f * relative + 0.01f,
                                        false,
                                        style = Stroke(5.dp.toPx()),
                                        size = iconSize * outlineFactor,
                                        topLeft = Offset(
                                            -0.5f * (outlineFactor - 1f) * iconSize.width,
                                            -0.5f * (outlineFactor - 1) * iconSize.height
                                        )
                                    )*/
                                }
                            }
                        })
                    }
                }
            }
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(text = serialized)
                }
            }
        }
    }


}
