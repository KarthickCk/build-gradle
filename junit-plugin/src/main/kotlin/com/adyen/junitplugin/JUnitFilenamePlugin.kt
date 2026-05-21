package com.adyen.junitplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

class JUnitFilenamePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("junit plugin applied to ${project.path}")

        val extension = project.extensions.create(
            "junitExtension",
            JUnitFilenameExtension::class.java
        )
        extension.outputDir.convention(
            project.layout.buildDirectory.dir("test-results-augmented")
        )

        val augmentTask = project.tasks.register(
            "augmentJUnitXml",
            AugmentJUnitXmlTask::class.java
        ) {
            outputDir.set(extension.outputDir)
            projectRootDir.set(project.rootDir)
        }

        project.afterEvaluate {
            val sourceSets = project.extensions
                .getByType(SourceSetContainer::class.java)
            val testSourceDirs = sourceSets
                .getByName("test")
                .allSource
                .srcDirs

            project.tasks.withType(Test::class.java).forEach { testTask ->
                augmentTask.configure {
                    inputDirs.from(testTask.reports.junitXml.outputLocation.get().asFile)
                    testSources.from(testSourceDirs)
                    mustRunAfter(testTask)
                }
                testTask.finalizedBy(augmentTask)
            }
        }
    }

}