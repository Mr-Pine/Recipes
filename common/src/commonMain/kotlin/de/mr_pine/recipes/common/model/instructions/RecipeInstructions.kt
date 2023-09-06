package de.mr_pine.recipes.common.model.instructions

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import de.mr_pine.recipes.common.model.MutableStateListSerializer
import de.mr_pine.recipes.common.model.MutableStateSerializer
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class RecipeInstructions(
    @Serializable(with = MutableStateListSerializer::class)
    var instructions: SnapshotStateList<RecipeInstruction>
) {

    constructor(instructions: List<RecipeInstruction>) : this(instructions.toMutableStateList())

    fun copy(instructions: List<RecipeInstruction> = this.instructions.map { it.copy() }) =
        RecipeInstructions(instructions)

    var currentlyActiveIndex by mutableIntStateOf(0)
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

    fun copy(
        content: AnnotatedString = this.content.copy(),
        inlineEmbeds: List<EmbedData> = this.inlineEmbeds.map { it.copy() }
    ) = RecipeInstruction(content, inlineEmbeds)

    @Serializable(with = EmbedDataSerializer::class)
    class EmbedData(
        @Transient
        private val enabledState: MutableState<Boolean> = mutableStateOf(true),
        @SerialName("embed")
        private val embedState: MutableState<InstructionSubmodels.EmbedTypeModel>
    ) {

        var enabled by enabledState

        constructor(enabled: Boolean, embed: InstructionSubmodels.EmbedTypeModel) : this(
            mutableStateOf(enabled),
            mutableStateOf(embed)
        )

        var embed by embedState

        fun copy(
            enabled: Boolean = this.enabled,
            embed: InstructionSubmodels.EmbedTypeModel = this.embed.copy()
        ): EmbedData = EmbedData(enabled, embed)
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
    /*val elements = encoded.split(regex = "[{][{]\\d+[}][}]".toRegex())
    elements.forEachIndexed { index, element ->
        append(element)
        if (index != elements.lastIndex) {
            appendInlineContent(index.toString())
        }
    }*/
    append(encoded)
    "[{][{]\\d+[}][}]".toRegex().findAll(encoded).forEach { matchResult ->
        addStringAnnotation(
            "androidx.compose.foundation.text.inlineContent",
            matchResult.value.substring(2, matchResult.value.length - 2),
            matchResult.range.first,
            matchResult.range.last + 1
        )
    }
}

fun encodeInstructionString(decoded: AnnotatedString): String {
    var text = decoded.text
    val annotations = decoded.getStringAnnotations(0, decoded.length)
    val hasIndices = !decoded.text.contains('�')
    val hasIndicesMultiplier = if(hasIndices) 0 else 1
    annotations.forEachIndexed { index, range ->
        text =
            text.replaceRange(range.start + 4 * index * hasIndicesMultiplier until range.end + 4 * index * hasIndicesMultiplier, "{{${if(hasIndices) text.substring(range.start + 2, range.end - 2) else index}}}")
    }
    return text
}


