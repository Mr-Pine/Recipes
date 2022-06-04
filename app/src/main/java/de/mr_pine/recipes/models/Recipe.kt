package de.mr_pine.recipes.models

private const val TAG = "Recipe"

data class Recipe(
    val metadata: RecipeMetadata,
    val ingredients: RecipeIngredients,
    val instructions: RecipeInstructions
) {


    companion object {
        fun deserialize(serialized: String): Recipe {
            val metadata =
                RecipeMetadata.deserialize(
                    serialized.extractData(RecipeMetadata.DataTag))
            val ingredients = RecipeIngredients(serialized.extractData(RecipeIngredients.DataTag))
            return Recipe(
                metadata = metadata,
                ingredients = ingredients,
                instructions = RecipeInstructions(serialized.extractData(RecipeInstructions.DataTag), metadata.title)
            )
        }
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

fun String.extractString(stringName: String): String {
    val start = "$stringName\\s*:\\s*\"".toRegex().find(this)
        ?: throw Exception("$stringName not contained in recipe")
    val end = "(?<!\\\\)\"".toRegex().find(this, start.range.last + 1)
        ?: throw Exception("Missing ending '\"'")
    return this.substring(start.range.last + 1, end.range.first)
}

interface RecipeList : RecipeDeserializable {
    val list: MutableList<RecipeDeserializable>
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

    fun deserialize(): RecipeDeserializable
}