package de.mr_pine.recipes.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.mr_pine.recipes.models.instructions.RecipeInstructions

private const val TAG = "Recipe"

class Recipe(
    val fileName: String = "",
    private val serializeMetadata: Boolean = true,
    override var serialized: String,
    private val initDeserialize: Boolean = false,
) : RecipeDeserializable {

    var instructions: RecipeInstructions? by mutableStateOf(null)
    var metadata: RecipeMetadata? by mutableStateOf(null)
    var ingredients: RecipeIngredients? by mutableStateOf(null)

    init {
        deserialize()
    }

    override fun deserialize(forceDeserialization: Boolean): Recipe {
        if(serializeMetadata || initDeserialize || forceDeserialization) metadata = RecipeMetadata(serialized.extractData(RecipeMetadata.DataTag))
        if(initDeserialize || forceDeserialization) {
            ingredients = RecipeIngredients(serialized.extractData(RecipeIngredients.DataTag))
            instructions = RecipeInstructions(
                serialized.extractData(RecipeInstructions.DataTag),
                metadata!!.title
            )
        }

        return this
    }
}

fun String.extractData(dataName: String): String {
    val start = "$dataName\\s*:\\s*[{]".toRegex().find(this)
        ?: throw Exception("$dataName not contained in recipe")
    val followingText = this.substring(start.range.last + 1)

    var openedBrackets = 1
    val regex = "(?<!\\\\)[{}]".toRegex()
    var match: MatchResult? = null
    while (openedBrackets > 0) {
        match = regex.find(followingText, (match?.range?.last ?: 0) + 1)
        openedBrackets += if (match != null && match.value == "{") 1 else -1
    }
    val end = match
    return followingText.substring(
        0,
        (end?.range?.first ?: throw Exception("Missing closing bracket")) - 1
    )
}

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