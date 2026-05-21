package com.adyen.junitplugin

import org.gradle.api.file.DirectoryProperty

interface JUnitFilenameExtension {
    val outputDir: DirectoryProperty
}