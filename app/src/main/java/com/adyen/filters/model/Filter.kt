package com.adyen.filters.model

data class Filter(
    val name: String,
    val includes: List<String>,
    val excludes: List<String>
)
