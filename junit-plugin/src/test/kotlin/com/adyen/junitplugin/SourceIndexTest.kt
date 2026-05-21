package com.adyen.junitplugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SourceIndexTest {

    @TempDir
    lateinit var projectRoot: File

    @Test
    fun `indexes a Kotlin file by package plus filename`() {
        val root = projectRoot.resolve("src/test/kotlin")
        writeFile(
            root, "com/adyen/FooTest.kt",
            """                                                                                                                                                               
              package com.adyen                                                                                                                                                 
              class FooTest                                                                                                                                                     
              """.trimIndent()
        )

        val index = SourceIndex.build(listOf(root), projectRoot)

        assertEquals(
            "src/test/kotlin/com/adyen/FooTest.kt",
            index.pathFor("com.adyen.FooTest")
        )
    }

    @Test
    fun `indexes a Java file the same way`() {
        val root = projectRoot.resolve("src/test/java")
        writeFile(
            root, "com/adyen/BarTest.java",
            """                                                                                                                                                               
              package com.adyen;
              public class BarTest {}                                                                                                                                           
              """.trimIndent()
        )

        val index = SourceIndex.build(listOf(root), projectRoot)

        assertEquals(
            "src/test/java/com/adyen/BarTest.java",
            index.pathFor("com.adyen.BarTest")
        )
    }

    @Test
    fun `returns null for unknown FQCN`() {
        val root = projectRoot.resolve("src/test/kotlin")
        writeFile(root, "com/adyen/Foo.kt", "package com.adyen\nclass Foo")

        val index = SourceIndex.build(listOf(root), projectRoot)

        assertNull(index.pathFor("com.other.Missing"))
    }

    @Test
    fun `skips files with no package declaration`() {
        val root = projectRoot.resolve("src/test/kotlin")
        writeFile(root, "TopLevel.kt", "class TopLevel")

        val index = SourceIndex.build(listOf(root), projectRoot)

        // No package = no FQCN can be assembled, so the class isn't indexed.
        assertNull(index.pathFor("TopLevel"))
    }

    @Test
    fun `silently ignores non-existent source roots`() {
        val real = projectRoot.resolve("src/test/kotlin")
        writeFile(real, "com/adyen/Foo.kt", "package com.adyen\nclass Foo")
        val missing = projectRoot.resolve("src/test/nope")

        // Should not throw; should still index files from the real root.
        val index = SourceIndex.build(listOf(real, missing), projectRoot)

        assertEquals(
            "src/test/kotlin/com/adyen/Foo.kt",
            index.pathFor("com.adyen.Foo")
        )
    }

    @Test
    fun `indexes files across multiple source roots`() {
        val kotlinRoot = projectRoot.resolve("src/test/kotlin")
        val javaRoot = projectRoot.resolve("src/test/java")
        writeFile(kotlinRoot, "com/adyen/KotlinTest.kt", "package com.adyen\nclass KotlinTest")
        writeFile(javaRoot, "com/adyen/JavaTest.java", "package com.adyen;\npublic class JavaTest {}")

        val index = SourceIndex.build(listOf(kotlinRoot, javaRoot), projectRoot)

        assertEquals(
            "src/test/kotlin/com/adyen/KotlinTest.kt",
            index.pathFor("com.adyen.KotlinTest")
        )
        assertEquals(
            "src/test/java/com/adyen/JavaTest.java",
            index.pathFor("com.adyen.JavaTest")
        )
    }

    @Test
    fun `ignores files with unsupported extensions`() {
        val root = projectRoot.resolve("src/test/kotlin")
        writeFile(root, "com/adyen/notes.md", "# package com.adyen\nclass Notes")
        writeFile(root, "com/adyen/Foo.kt", "package com.adyen\nclass Foo")

        val index = SourceIndex.build(listOf(root), projectRoot)

        assertEquals("src/test/kotlin/com/adyen/Foo.kt", index.pathFor("com.adyen.Foo"))
        assertNull(index.pathFor("com.adyen.notes"))
    }

    private fun writeFile(root: File, relativePath: String, content: String) {
        val file = root.resolve(relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}