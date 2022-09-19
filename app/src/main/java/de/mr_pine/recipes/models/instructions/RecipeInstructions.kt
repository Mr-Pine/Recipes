package de.mr_pine.recipes.models.instructions

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import de.mr_pine.recipes.models.MutableStateListSerializer
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
    @Serializable(with = MutableStateListSerializer::class)
    @SerialName("replacements")
    val inlineEmbeds: SnapshotStateList<EmbedData> = mutableStateListOf()
) : InstructionSubmodels {

    var done by mutableStateOf(false)

    @Serializable(with = EmbedDataSerializer::class)
    data class EmbedData(
        @Transient var enabled: Boolean = true,
        @SerialName("embed")
        private var embedState: MutableState<InstructionSubmodels.EmbedTypeModel>
    ) {
        var embed by embedState
    }

    private object EmbedDataSerializer : KSerializer<EmbedData> {
        override fun deserialize(decoder: Decoder): EmbedData {
            return EmbedData(
                embedState = mutableStateOf(serializer<InstructionSubmodels.EmbedTypeModel>().deserialize(decoder))
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
        val unannotated = decoder.decodeString()
        return decodeInstructionString(unannotated)
    }

    override fun serialize(encoder: Encoder, value: AnnotatedString) {

        encoder.encodeString(encodeInstructionString(value))
    }
}

fun decodeInstructionString(encoded: String) = buildAnnotatedString {
    val elements = encoded.split(regex = "[{][{]\\d+[}][}]".toRegex())
    elements.forEachIndexed { index, element ->
        append(element)
        if (index != elements.lastIndex) {
            appendInlineContent(index.toString())
        }
    }
}

fun encodeInstructionString(decoded: AnnotatedString): String {
    var text = decoded.text
    val annotations = decoded.getStringAnnotations(0, decoded.lastIndex)
    annotations.forEachIndexed { index, range ->
        text =
            text.replaceRange(range.start + 4 * index until range.end + 4 * index, "{{$index}}")
    }
    return text
}


