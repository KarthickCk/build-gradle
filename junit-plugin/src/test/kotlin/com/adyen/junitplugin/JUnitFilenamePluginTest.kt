package com.adyen.junitplugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class JUnitFilenamePluginUnitTest {

    @Test
    fun `registers the junitFilename extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.adyen.junit-plugin")

        val extension = project.extensions.findByName("junitExtension")
        assertNotNull(extension, "Extension 'junitExtension' was not registered")
        assertTrue(
            extension is JUnitFilenameExtension,
            "Extension is not of type junitExtension"
        )
    }

    @Test
    fun `registers the augmentJUnitXml task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.adyen.junit-plugin")

        val task = project.tasks.findByName("augmentJUnitXml")
        assertNotNull(task, "Task 'augmentJUnitXml' was not registered")
        assertTrue(
            task is AugmentJUnitXmlTask,
            "Task is not of type AugmentJUnitXmlTask"
        )
    }

    @Test
    fun `default outputDir is build slash test-results-augmented`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.adyen.junit-plugin")

        val extension = project.extensions.getByType(JUnitFilenameExtension::class.java)
        val resolved = extension.outputDir.get().asFile

        assertEquals(
            project.layout.buildDirectory.dir("test-results-augmented").get().asFile,
            resolved
        )
    }

    @Test
    fun `consumer-set outputDir overrides convention`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.adyen.junit-plugin")

        val extension = project.extensions.getByType(JUnitFilenameExtension::class.java)
        val custom = project.layout.buildDirectory.dir("custom-augmented")
        extension.outputDir.set(custom)

        assertEquals(
            custom.get().asFile,
            extension.outputDir.get().asFile
        )
    }
}