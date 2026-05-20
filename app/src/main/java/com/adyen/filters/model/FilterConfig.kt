package com.adyen.filters.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterConfig(
    val filters: Map<String, List<String>>
)
