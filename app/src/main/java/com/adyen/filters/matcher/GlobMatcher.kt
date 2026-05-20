package com.adyen.filters.matcher

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class GlobMatcher {

    private val cache = mutableMapOf<String, PathMatcher>()

    fun matches(pattern: String, filePath: String): Boolean {
        val path = Path.of(filePath)
        return expandVariants(pattern).any { compile(it).matches(path) }
    }

    private fun expandVariants(pattern: String): Set<String> {
        val variants = mutableSetOf(pattern)

        // "**/foo" → also try "foo" (handles zero leading segments)
        if (pattern.startsWith("**/")) {
            variants += expandVariants(pattern.removePrefix("**/"))
        }

        // "a/**/b" → also try "a/b" (handles zero middle segments)
        if (pattern.contains("/**/")) {
            variants += expandVariants(pattern.replace("/**/", "/"))
        }

        return variants
    }

    private fun compile(pattern: String): PathMatcher =
        cache.getOrPut(pattern) {
            FileSystems.getDefault().getPathMatcher("glob:$pattern")
        }
}