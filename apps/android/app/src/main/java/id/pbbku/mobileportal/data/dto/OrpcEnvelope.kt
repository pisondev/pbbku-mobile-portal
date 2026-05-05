package id.pbbku.mobileportal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrpcRequest<T>(
    val json: T,
)

@Serializable
data class OrpcResponse<T>(
    val json: T? = null,
)

@Serializable
class EmptyRequest
