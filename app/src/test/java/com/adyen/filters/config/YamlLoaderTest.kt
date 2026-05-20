package com.adyen.filters.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.emptyList

class YamlLoaderTest {

    private val loader = YamlLoader()

    @Test fun `parses single filter with include only`(@TempDir tmp: Path) {
        val yaml = tmp.resolve("filters.yaml").also {
            Files.writeString(it, """                                                                                                                                         
                  filters:
                    backend:                                                                                                                                                    
                      - "**/*.kt"                                                                                                                                             
              """.trimIndent())
        }

        val filters = loader.load(yaml)

        assertEquals(1, filters.size)
        assertEquals("backend", filters[0].name)
        assertEquals(listOf("**/*.kt"), filters[0].includes)
        assertEquals(emptyList<String>(), filters[0].excludes)
    }

    @Test fun `splits include and exclude patterns`(@TempDir tmp: Path) {
        val yaml = tmp.resolve("filters.yaml").also {
            Files.writeString(it, """                                                                                                                                         
                  filters:                                                                                                                                                    
                    backend:                                                                                                                                                    
                      - "app/src/**/*.kt"
                      - "!app/src/test/**"                                                                                                                                      
                      - "!**/generated/**"                                                                                                                                    
              """.trimIndent())
        }

        val filters = loader.load(yaml)

        val backend = filters.single()
        assertEquals(listOf("app/src/**/*.kt"), backend.includes)
        assertEquals(listOf("app/src/test/**", "**/generated/**"), backend.excludes)
    }

    @Test fun `parses multiple filters`(@TempDir tmp: Path) {
        val yaml = tmp.resolve("filters.yaml").also {
            Files.writeString(it, """                                                                                                                                         
                  filters:                                                                                                                                                    
                    backend:                                                                                                                                                    
                      - "**/*.kt"
                    frontend:                                                                                                                                                   
                      - "web/**/*.ts"                                                                                                                                         
                    docs:
                      - "README.md"
              """.trimIndent())
        }

        val filters = loader.load(yaml)

        assertEquals(3, filters.size)
        assertEquals(setOf("backend", "frontend", "docs"), filters.map { it.name }.toSet())
    }
}