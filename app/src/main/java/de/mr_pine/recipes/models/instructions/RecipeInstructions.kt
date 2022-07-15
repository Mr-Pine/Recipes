package de.mr_pine.recipes.models.instructions

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val TAG = "RecipeInstructions"

@Serializable
class RecipeInstructions(
    var instructions: List<RecipeInstruction>
) {

    var currentlyActiveIndex by mutableStateOf(0)

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
    val inlineEmbeds: List<EmbedData> = listOf()
) : InstructionSubmodels {

    var done by mutableStateOf(false)

    @Serializable(with = EmbedDataSerializer::class)
    data class EmbedData(
        @Transient var enabled: Boolean = true,
        val embed: InstructionSubmodels.EmbedTypeModel
    )

    private object EmbedDataSerializer : KSerializer<EmbedData> {
        override fun deserialize(decoder: Decoder): EmbedData {
            return EmbedData(
                embed = serializer<InstructionSubmodels.EmbedTypeModel>().deserialize(decoder)
            )
        }

        override val descriptor: SerialDescriptor =
            serializer<InstructionSubmodels.EmbedTypeModel>().descriptor

        override fun serialize(encoder: Encoder, value: EmbedData) {
            serializer<InstructionSubmodels.EmbedTypeModel>().serialize(encoder, value.embed)
        }
    }
}

object AnnotatedSerializer : KSerializer<AnnotatedString> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AnnotatedString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AnnotatedString {
        return buildAnnotatedString {
            val unannotated = decoder.decodeString()
            val elements = unannotated.split(regex = "[{][{]\\d+[}][}]".toRegex())
            elements.forEachIndexed { index, element ->
                append(element)
                if (index != elements.lastIndex) {
                    appendInlineContent(index.toString())
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: AnnotatedString) {
        var text = value.text
        val annotations = value.getStringAnnotations(0, value.lastIndex)
        annotations.forEachIndexed { index, range ->
            text = text.replaceRange(range.start..range.end, "{{$index}}")
        }
        encoder.encodeString(text)
    }
}



