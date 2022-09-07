package de.mr_pine.recipes.models

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
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