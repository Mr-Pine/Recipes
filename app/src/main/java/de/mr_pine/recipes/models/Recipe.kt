package de.mr_pine.recipes.models

import de.mr_pine.recipes.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.models.instructions.RecipeInstruction
import de.mr_pine.recipes.models.instructions.RecipeInstructions
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
data class Recipe(
    val instructions: RecipeInstructions,
    val metadata: RecipeMetadata,
    val ingredients: RecipeIngredients
)

@ExperimentalSerializationApi
object RecipeSerializer: KSerializer<Recipe> {
    @Serializable
    data class RecipeBackbone(
        val metadata: RecipeMetadata,
        val ingredients: List<RecipeIngredient>,
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
        encoder.encodeSerializableValue(RecipeBackbone.serializer(), RecipeBackbone(value.metadata, value.ingredients.ingredients, value.instructions.instructions))
    }
}

fun String.extractString(stringName: String, enclosing: Char = '"'): String {
    val start = "$stringName\\s*:\\s*$enclosing".toRegex().find(this)
        ?: throw Exception("$stringName not contained in $this")
    val end = "(?<!\\\\)$enclosing".toRegex().find(this, start.range.last + 1)
        ?: throw Exception("Missing ending '\"'")
    return this.substring(start.range.last + 1, end.range.first)
}