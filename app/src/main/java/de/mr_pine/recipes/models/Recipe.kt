package de.mr_pine.recipes.models

import de.mr_pine.recipes.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.models.instructions.RecipeInstructions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
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
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Recipe") {
        element("instructions", RecipeInstructions.serializer().descriptor)
        element("metadata", RecipeMetadata.serializer().descriptor)
        element("ingredients", RecipeIngredients.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): Recipe {
        return Recipe(
            instructions = RecipeInstructions.serializer().deserialize(decoder),
            metadata = RecipeMetadata.serializer().deserialize(decoder),
            ingredients = RecipeIngredients.serializer().deserialize(decoder)
        )
    }

    override fun serialize(encoder: Encoder, value: Recipe) {
        TODO("Not yet implemented")
    }
}

fun String.extractString(stringName: String, enclosing: Char = '"'): String {
    val start = "$stringName\\s*:\\s*$enclosing".toRegex().find(this)
        ?: throw Exception("$stringName not contained in $this")
    val end = "(?<!\\\\)$enclosing".toRegex().find(this, start.range.last + 1)
        ?: throw Exception("Missing ending '\"'")
    return this.substring(start.range.last + 1, end.range.first)
}