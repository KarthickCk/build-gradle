package com.adyen.junitplugin

import java.io.File
import java.nio.file.Path

fun main() {
    val index = SourceIndex.build(
        sourceRoots = listOf(File("app/src/test/kotlin"), File("app/src/test/java")),
        projectRoot = File(".")
    )
    println(index.pathFor("com.adyen.filters.matcher.GlobMatcherTest"))
}

class SourceIndex private constructor(
    private val fqcnToRelativePath: Map<String, String>
) {
    fun pathFor(fqcn: String): String? = fqcnToRelativePath[fqcn]

    companion object {
        private val PACKAGE_REGEX = Regex("""^\s*package\s+([\w.]+)""")
        private val SUPPORTED_EXTENSIONS = setOf("kt", "java")

        fun build(sourceRoots: Iterable<File>, projectRoot: File): SourceIndex {
            val rootPath: Path = projectRoot.toPath()
            val index = mutableMapOf<String, String>()

            sourceRoots.forEach { root ->
                if (!root.isDirectory) return@forEach
                root.walkTopDown()
                    .filter { it.isFile && it.extension in SUPPORTED_EXTENSIONS }
                    .forEach { file ->
                        val pkg = readPackage(file) ?: return@forEach
                        val fqcn = "$pkg.${file.nameWithoutExtension}"
                        val rel = rootPath.relativize(file.toPath()).toString()
                        index[fqcn] = rel
                    }
            }

            return SourceIndex(index)
        }

        private fun readPackage(file: File): String? {
            file.useLines { lines ->
                for (line in lines.take(50)) {
                    PACKAGE_REGEX.find(line)?.let { return it.groupValues[1] }
                }
            }
            return null
        }
    }
}