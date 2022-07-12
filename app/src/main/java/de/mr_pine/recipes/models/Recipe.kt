package de.mr_pine.recipes.models

import de.mr_pine.recipes.models.instructions.RecipeInstructions
import kotlinx.serialization.Serializable

private const val TAG = "Recipe"

@Serializable
data class Recipe(
    val instructions: RecipeInstructions,
    val metadata: RecipeMetadata,
    val ingredients: RecipeIngredients
)

fun String.extractString(stringName: String, enclosing: Char = '"'): String {
    val start = "$stringName\\s*:\\s*$enclosing".toRegex().find(this)
        ?: throw Exception("$stringName not contained in $this")
    val end = "(?<!\\\\)$enclosing".toRegex().find(this, start.range.last + 1)
        ?: throw Exception("Missing ending '\"'")
    return this.substring(start.range.last + 1, end.range.first)
}

fun String.extractFromList(): MutableList<String> {
    val items = mutableListOf<String>()
    var lastIndex = 0
    do {
        val start = "(?<!\\\\)\\[".toRegex().find(this, lastIndex)
        var end: MatchResult?
        if (start != null) {
            end = "(?<!\\\\)]".toRegex().find(this, start.range.last + 1)
                ?: throw Exception("Missing ending '\"'")
            items.add(
                this.substring(
                    start.range.last + 1,
                    end.range.first
                )
            )
            lastIndex = end.range.last
        }
    } while (start != null)
    return items
}

interface RecipeDeserializable {

    val serialized: String

    fun deserialize(forceDeserialization: Boolean = false): RecipeDeserializable
}