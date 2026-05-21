package com.adyen.filters

import com.adyen.filters.config.YamlLoader
import com.adyen.filters.matcher.FilterEvaluator
import com.adyen.filters.matcher.GlobMatcher
import com.adyen.filters.output.EnvWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path

fun main(args: Array<String>) = FilterCli().main(args)

class FilterCli : CliktCommand(name = "filters") {

    private val config by option("--config", help = "Path to filters.yaml")
        .path(mustExist = true, canBeDir = false)
        .required()

    private val output by option("--output", help = "Path to write the .env file")
        .path()
        .default(Path.of(".env"))

    private val files by option("--files", help = "Changed file paths (space-separated)")
        .split(" ")
        .transformAll { it.flatten().map(String::trim).filter(String::isNotBlank) }

    override fun run() {
        val filters = YamlLoader().load(config)
        val evaluator = FilterEvaluator(GlobMatcher())
        val cleanFiles = files.map(String::trim).filter(String::isNotBlank)
        val results = evaluator.evaluate(filters, cleanFiles)

        EnvWriter().write(results, output)

        echo("Filter evaluation:")
        results.forEach { (name, value) -> echo("  $name = $value") }
        echo("Output written to: $output")
    }
}