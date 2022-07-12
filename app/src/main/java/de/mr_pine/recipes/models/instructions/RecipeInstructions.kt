package de.mr_pine.recipes.models.instructions

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val TAG = "RecipeInstructions"

@Serializable
class RecipeInstructions(
    var instructions: List<RecipeInstruction>
) {

    var currentlyActiveIndex = mutableStateOf(0)

    companion object {
        const val DataTag = "Instructions"
    }
}

@Serializable
class RecipeInstruction(
    @SerialName("text")
    @Serializable(with = AnnotatedSerializer::class)
    val content: AnnotatedString,
    @SerialName("replacements")
    val inlineEmbeds: List<EmbedData>
) : InstructionSubmodels {

    var done by mutableStateOf(false)

    @Serializable
    data class EmbedData(@Transient var enabled: Boolean = true, var embed: InstructionSubmodels.EmbedTypeModel)
}

object AnnotatedSerializer: KSerializer<AnnotatedString> {
    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): AnnotatedString {
        return buildAnnotatedString {
            val unannotated = decoder.decodeString()
            val elements = unannotated.split(regex = "[{][{]\\d+[}][}]".toRegex())
            elements.forEachIndexed { index, element ->
                append(element)
                if(index != elements.lastIndex){
                    appendInlineContent(index.toString())
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: AnnotatedString) {
        TODO("Not yet implemented")
    }
}



