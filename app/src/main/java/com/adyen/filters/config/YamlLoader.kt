package com.adyen.filters.config

import com.adyen.filters.model.Filter
import com.adyen.filters.model.FilterConfig
import com.charleskorn.kaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

class YamlLoader {

    fun load(yamlPath: Path): List<Filter> {
        val text = Files.readString(yamlPath)
        val config = Yaml.default.decodeFromString(FilterConfig.serializer(), text)
        return toFilters(config)
    }

    private fun toFilters(config: FilterConfig): List<Filter> =
        config.filters.map { (name, patterns) ->
            val (excludes, includes) = patterns.partition { it.startsWith("!") }
            Filter(
                name     = name,
                includes = includes,
                excludes = excludes.map { it.removePrefix("!") }
            )
        }
}