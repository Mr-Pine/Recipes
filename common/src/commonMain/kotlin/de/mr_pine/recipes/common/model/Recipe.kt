package de.mr_pine.recipes.common.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import de.mr_pine.recipes.common.model.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.model.instructions.RecipeInstruction
import de.mr_pine.recipes.common.model.instructions.RecipeInstructions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private const val TAG = "Recipe"

val module = SerializersModule {
    polymorphic(InstructionSubmodels.EmbedTypeModel::class) {
        subclass(InstructionSubmodels.IngredientModel::class)
        subclass(InstructionSubmodels.TimerModel::class)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = RecipeSerializer::class)
class Recipe(
    @Serializable(with = MutableStateSerializer::class)
    private val instructionsState: MutableState<RecipeInstructions>,
    @Serializable(with = MutableStateSerializer::class)
    private val metadataState: MutableState<RecipeMetadata>,
    @Serializable(with = MutableStateSerializer::class)
    private val ingredientsState: MutableState<RecipeIngredients>
) {
    var instructions by instructionsState
    var metadata by metadataState
    var ingredients by ingredientsState

    constructor(
        instructions: RecipeInstructions,
        metadata: RecipeMetadata,
        ingredients: RecipeIngredients
    ) : this(
        mutableStateOf(instructions),
        mutableStateOf(metadata),
        mutableStateOf(ingredients)
    )

    fun copyFrom(other: Recipe) {
        instructions = other.instructions.copy()
        metadata = other.metadata.copy()
        ingredients = other.ingredients.copy()
    }

    fun copy() = Recipe(
        instructions.copy(),
        metadata.copy(),
        ingredients.deepCopy()
    )
}

@ExperimentalSerializationApi
object RecipeSerializer : KSerializer<Recipe> {
    @Serializable
    data class RecipeBackbone(
        val metadata: RecipeMetadata,
        @Serializable(with = MutableStateListSerializer::class)
        val ingredients: SnapshotStateList<RecipeIngredient>,
        val instructions: List<RecipeInstruction>
    )

    override val descriptor: SerialDescriptor = RecipeBackbone.serializer().descriptor

    override fun deserialize(decoder: Decoder): Recipe {
        val backbone = decoder.decodeSerializableValue(RecipeBackbone.serializer())
        return Recipe(
            instructions = RecipeInstructions(backbone.instructions),
            metadata = backbone.metadata,
            ingredients = RecipeIngredients(backbone.ingredients)
        )
    }

    override fun serialize(encoder: Encoder, value: Recipe) {
        encoder.encodeSerializableValue(
            RecipeBackbone.serializer(),
            RecipeBackbone(
                value.metadata,
                value.ingredients.ingredients,
                value.instructions.instructions
            )
        )
    }
}