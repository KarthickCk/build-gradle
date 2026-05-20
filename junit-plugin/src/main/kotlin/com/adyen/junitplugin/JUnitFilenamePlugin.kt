package com.adyen.junitplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class JUnitFilenamePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("junit plugin applied to ${project.path}")
    }
}