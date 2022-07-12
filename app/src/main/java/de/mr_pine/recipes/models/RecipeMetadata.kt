package de.mr_pine.recipes.models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TAG = "RecipeMetadata"

@Serializable
data class RecipeMetadata(
    var title: String,
    var author: String? = null,
    @SerialName("portions")
    var portionSize: Float? = null
) {

    companion object {
        const val DataTag = "Metadata"
    }
}