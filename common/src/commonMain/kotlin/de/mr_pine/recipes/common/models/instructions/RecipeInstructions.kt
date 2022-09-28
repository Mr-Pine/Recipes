package de.mr_pine.recipes.common.models.instructions

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import de.mr_pine.recipes.common.models.MutableStateListSerializer
import de.mr_pine.recipes.common.models.MutableStateSerializer
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val TAG = "RecipeInstructions"

@Serializable
class RecipeInstructions(
    @Serializable(with = MutableStateListSerializer::class)
    var instructions: SnapshotStateList<RecipeInstruction>
) {

    constructor(instructions: List<RecipeInstruction>) : this(instructions.toMutableStateList())

    fun copy(instructions: List<RecipeInstruction> = this.instructions.map { it.copy() }) =
        RecipeInstructions(instructions)

    var currentlyActiveIndex by mutableStateOf(0)

    companion object {
        const val DataTag = "Instructions"
    }
}

@Serializable
class RecipeInstruction(
    @SerialName("text")
    @Serializable(with = MutableStateSerializer::class)
    val contentState: MutableState<@Serializable(with = AnnotatedSerializer::class) AnnotatedString>,
    @Serializable(with = MutableStateListSerializer::class)
    @SerialName("replacements")
    val inlineEmbeds: SnapshotStateList<EmbedData> = mutableStateListOf()
) : InstructionSubmodels {

    constructor(
        content: AnnotatedString,
        inlineEmbeds: List<EmbedData>
    ) : this(mutableStateOf(content), inlineEmbeds.toMutableStateList())

    var done by mutableStateOf(false)

    var content by contentState

    fun copy(content: AnnotatedString = this.content.copy(), inlineEmbeds: List<EmbedData> = this.inlineEmbeds.map { it.copy() }) = RecipeInstruction(content, inlineEmbeds)

    @Serializable(with = EmbedDataSerializer::class)
    class EmbedData(
        @Transient var enabled: Boolean = true,
        @SerialName("embed")
        private var embedState: MutableState<InstructionSubmodels.EmbedTypeModel>
    ) {
        constructor(enabled: Boolean, embed: InstructionSubmodels.EmbedTypeModel): this(enabled, mutableStateOf(embed))

        var embed by embedState

        fun copy(enabled: Boolean = this.enabled, embed: InstructionSubmodels.EmbedTypeModel = this.embed.copy()): EmbedData = EmbedData(enabled, embed)
    }

    private object EmbedDataSerializer : KSerializer<EmbedData> {
        override fun deserialize(decoder: Decoder): EmbedData {
            return EmbedData(
                embedState = mutableStateOf(
                    serializer<InstructionSubmodels.EmbedTypeModel>().deserialize(
                        decoder
                    )
                )
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

fun AnnotatedString.copy() = buildAnnotatedString {
    append(this@copy)
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
    val annotations = decoded.getStringAnnotations(0, decoded.length)
    annotations.forEachIndexed { index, range ->
        text =
            text.replaceRange(range.start + 4 * index until range.end + 4 * index, "{{$index}}")
    }
    return text
}


