package com.adyen.filters.matcher

import com.adyen.filters.model.Filter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

class FilterEvaluatorTest {

    private val evaluator = FilterEvaluator(GlobMatcher())

    @Test
    fun `single inclusion match returns true`() {
        val filter = Filter("backend", includes = listOf("**/*.kt"), excludes = emptyList())
        val result = evaluator.evaluate(listOf(filter), listOf("app/Main.kt"))
        assertEquals(mapOf("backend" to true), result)
    }

    @Test
    fun `no matching files returns false`() {
        val filter = Filter("backend", includes = listOf("**/*.kt"), excludes = emptyList())
        val result = evaluator.evaluate(listOf(filter), listOf("web/index.ts"))
        assertEquals(mapOf("backend" to false), result)
    }

    @Test
    fun `exclusion overrides inclusion for the same file`() {
        val filter = Filter(
            name = "kotlin_sources",
            includes = listOf("**/*.kt"),
            excludes = listOf("**/test/**")
        )
        val result = evaluator.evaluate(filter, listOf("app/test/FooTest.kt"))
        assertFalse(result, "Excluded file should not satisfy the filter")
    }

    @Test
    fun `file matching include but not exclude returns true`() {
        val filter = Filter(
            name = "kotlin_sources",
            includes = listOf("**/*.kt"),
            excludes = listOf("**/test/**")
        )
        val result = evaluator.evaluate(filter, listOf("app/main/Foo.kt"))
        assertTrue(result, "Non-test Kotlin file should satisfy the filter")
    }

    @Test
    fun `at least one matching file is enough`() {
        val filter = Filter("backend", includes = listOf("**/*.kt"), excludes = emptyList())
        val files = listOf("web/index.ts", "docs/readme.md", "app/Main.kt")
        val result = evaluator.evaluate(filter, files)
        assertTrue(result, "Any matching file should satisfy the filter")
    }

    @Test
    fun `filter with empty includes is always false`() {
        // Mirrors paths-filter behavior: exclusions only subtract from inclusions,
        // so without any inclusion the filter can never match.
        val filter = Filter(
            name = "exclude_only",
            includes = emptyList(),
            excludes = listOf("**/test/**")
        )
        val result = evaluator.evaluate(filter, listOf("app/main/Foo.kt"))
        assertFalse(result)
    }

    @Test
    fun `multiple filters evaluated independently`() {
        val filters = listOf(
            Filter("backend",  includes = listOf("**/*.kt"),    excludes = emptyList()),
            Filter("frontend", includes = listOf("web/**/*.ts"), excludes = emptyList()),
            Filter("docs",     includes = listOf("docs/**"),     excludes = emptyList())
        )
        val files = listOf("app/Main.kt", "web/index.ts")
        val result = evaluator.evaluate(filters, files)

        assertEquals(
            mapOf("backend" to true, "frontend" to true, "docs" to false),
            result
        )
    }

    @Test
    fun `empty changed files returns false for all filters`() {
        val filters = listOf(
            Filter("backend",  includes = listOf("**/*.kt"), excludes = emptyList()),
            Filter("frontend", includes = listOf("**/*.ts"), excludes = emptyList())
        )
        val result = evaluator.evaluate(filters, emptyList())
        assertEquals(
            mapOf("backend" to false, "frontend" to false),
            result
        )
    }

    @Test
    fun `multiple include patterns - any match qualifies`() {
        val filter = Filter(
            name = "frontend",
            includes = listOf("web/**/*.ts", "web/**/*.tsx"),
            excludes = emptyList()
        )
        val result = evaluator.evaluate(filter, listOf("web/components/Button.tsx"))
        assertTrue(result, "Match against any include pattern should qualify")
    }

    @Test
    fun `multiple exclude patterns - any match disqualifies`() {
        val filter = Filter(
            name = "kotlin_sources",
            includes = listOf("**/*.kt"),
            excludes = listOf("**/test/**", "**/generated/**")
        )
        assertFalse(evaluator.evaluate(filter, listOf("app/test/FooTest.kt")))
        assertFalse(evaluator.evaluate(filter, listOf("app/generated/Bar.kt")))
        assertTrue(evaluator.evaluate(filter, listOf("app/main/Real.kt")))
    }

    private fun FilterEvaluator.evaluate(filter: Filter, files: List<String>): Boolean =
        evaluate(listOf(filter), files).getValue(filter.name)
}