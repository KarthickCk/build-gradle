package com.adyen.filters.matcher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GlobMatcherTest {

    private val matcher = GlobMatcher()

    @Test
    fun `single star matches within one segment only`() {
        assertTrue(matcher.matches("*.kt", "Main.kt"))
        assertFalse(matcher.matches("*.kt", "src/Main.kt"))
    }

    @Test
    fun `double star matches at root and nested`() {
        assertTrue(matcher.matches("**/*.kt", "Main.kt"))
        assertTrue(matcher.matches("**/*.kt", "src/Main.kt"))
        assertTrue(matcher.matches("**/*.kt", "a/b/c/Main.kt"))
        assertFalse(matcher.matches("**/*.kt", "Main.java"))
    }

    @Test
    fun `redundant double star prefixes still match`() {
        assertTrue(matcher.matches("**/**/*.kt", "Main.kt"))
        assertTrue(matcher.matches("**/**/**/*.kt", "a/Main.kt"))
    }

    @Test
    fun `mid-pattern double star matches zero segments`() {
        assertTrue(matcher.matches("data/**/*.kt", "data/Key.kt"))
        assertTrue(matcher.matches("data/**/*.kt", "data/sub/Key.kt"))
        assertTrue(matcher.matches("data/**/*.kt", "data/a/b/Key.kt"))
        assertFalse(matcher.matches("data/**/*.kt", "other/Key.kt"))
        assertFalse(matcher.matches("data/**/*.kt", "Key.kt"))
    }

    @Test
    fun `combined leading and mid double star`() {
        assertTrue(matcher.matches("**/data/**/*.kt", "data/Key.kt"))
        assertTrue(matcher.matches("**/data/**/*.kt", "a/data/Key.kt"))
        assertTrue(matcher.matches("**/data/**/*.kt", "a/data/b/Key.kt"))
    }

    @Test
    fun `docs pattern matches root and nested md files`() {
        assertTrue(matcher.matches("docs/**/*.md", "docs/README.md"))
        assertTrue(matcher.matches("docs/**/*.md", "docs/api/usage.md"))
        assertTrue(matcher.matches("docs/**/*.md", "docs/api/v1/users.md"))
        assertFalse(matcher.matches("docs/**/*.md", "OTHER.md"))
        assertFalse(matcher.matches("docs/**/*.md", "docs/overview.txt"))
    }

    @Test
    fun `directory wildcard matches anything under directory`() {
        assertTrue(matcher.matches("**/test/**", "test/Foo.kt"))
        assertTrue(matcher.matches("**/test/**", "app/src/test/Foo.kt"))
        assertFalse(matcher.matches("**/test/**", "app/src/Main.kt"))
    }

    @Test
    fun `exact file name match`() {
        assertTrue(matcher.matches("README.md", "README.md"))
        assertFalse(matcher.matches("README.md", "OTHER.md"))
        assertFalse(matcher.matches("README.md", "docs/README.md"))
    }
}