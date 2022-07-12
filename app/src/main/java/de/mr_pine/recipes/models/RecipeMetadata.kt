package de.mr_pine.recipes.models

import android.util.Log

private const val TAG = "RecipeMetadata"

class RecipeMetadata(
    override val serialized: String
) : RecipeDeserializable {
    var title: String = ""
    var author: String? = null
    var portionSize: Float? = null

    companion object {
        const val DataTag = "Metadata"
    }

    init {
        deserialize()
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeMetadata {
        title = serialized.extractString("Title")
        try {
            author = serialized.extractString("Author")
        } catch (e: Exception) {
            Log.i(TAG, "deserialize: No author found")
        }
        try {
            portionSize = serialized.extractString("Portions").toFloat()
        } catch (e: Exception) {
            Log.i(TAG, "deserialize: No portionsize found or not a number")
        }
        return this
    }
}