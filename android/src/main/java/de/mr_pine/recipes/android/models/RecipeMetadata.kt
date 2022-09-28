package de.mr_pine.recipes.android.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

private const val TAG = "RecipeMetadata"

@Serializable
data class RecipeMetadata(
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("title")
    private val titleState: MutableState<String>,
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("author")
    private val authorState: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    @SerialName("portions")
    private val portionSizeState: MutableState<Float?> = mutableStateOf(null),
    @Transient
    var file: File? = null
) {
    var title by titleState
    var author by authorState
    var portionSize by portionSizeState

    constructor(
        title: String,
        author: String? = null,
        portionSize: Float? = null,
        file: File? = null
    ) : this(
        mutableStateOf(title), mutableStateOf(author), mutableStateOf(portionSize), file
    )

    fun copyFrom(other: RecipeMetadata)  {
        this.title = other.title
        this.author = other.author
        this.portionSize = other.portionSize
        this.file = other.file
    }

    fun copy(): RecipeMetadata {
        val temp = RecipeMetadata("")
        temp.copyFrom(this)
        return temp
    }

    companion object {
        const val DataTag = "Metadata"
    }
}