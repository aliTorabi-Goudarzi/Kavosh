package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class StorageInfo(
    val total: String = "0 GB",
    val available: String = "0 GB"
)