package com.adyen.filters.output

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

class EnvWriter {

    fun write(results: Map<String, Boolean>, outputPath: Path) {
        outputPath.createParentDirectories()
        val content = results.entries.joinToString("\n") { (name, value) -> "$name=$value" }
        Files.writeString(outputPath, content + "\n")
    }
}