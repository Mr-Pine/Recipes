package de.mr_pine.recipes.common.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class MutableStateListSerializer<T>(dataSerializer: KSerializer<T>): KSerializer<SnapshotStateList<T>> {
    private val listSerializer = ListSerializer(dataSerializer)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): SnapshotStateList<T> {
        return listSerializer.deserialize(decoder).toMutableStateList()
    }

    override fun serialize(encoder: Encoder, value: SnapshotStateList<T>) {
        listSerializer.serialize(encoder, value)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = MutableState::class)
class MutableStateSerializer<T>(private val dataSerializer: KSerializer<T>): KSerializer<MutableState<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor

    override fun deserialize(decoder: Decoder): MutableState<T> {
        return mutableStateOf(dataSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: MutableState<T>) {
        dataSerializer.serialize(encoder, value.value)
    }
}