package id.pbbku.mobileportal.domain.model

import kotlinx.serialization.json.JsonElement

data class ApiPayload(
    val endpoint: String,
    val json: JsonElement,
)
