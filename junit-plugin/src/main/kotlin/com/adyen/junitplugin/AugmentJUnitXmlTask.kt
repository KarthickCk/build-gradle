package com.adyen.junitplugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

abstract class AugmentJUnitXmlTask : DefaultTask() {

    companion object {
        private val STACK_FRAME_REGEX =
            Regex("""\s+at\s+(?:[\w.@]+//)?([\w.$]+?)\.[\w$<>]+\(([^:)]+):(\d+)\)""")
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val testSources: ConfigurableFileCollection

    @get:Internal
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun augment() {
        val target = outputDir.get().asFile
        target.mkdirs()

        val index = SourceIndex.build(
            sourceRoots = testSources.files,
            projectRoot = projectRootDir.get().asFile
        )

        var processed = 0
        inputDirs.files.forEach { dir ->
            if (!dir.isDirectory) return@forEach
            dir.listFiles { f -> f.isFile && f.extension == "xml" }
                ?.forEach { xml ->
                    augmentFile(xml, target.resolve(xml.name), index)
                    processed++
                }
        }

        logger.lifecycle("Augmented $processed XML file(s) into $target")
    }

    private fun augmentFile(source: File, destination: File, index: SourceIndex) {
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        val doc = factory.newDocumentBuilder().parse(source)

        val testcases = doc.getElementsByTagName("testcase")
        for (i in 0 until testcases.length) {
            val node = testcases.item(i) as? Element ?: continue
            val classname = node.getAttribute("classname").takeIf { it.isNotBlank() } ?: continue
            val relPath = index.pathFor(classname) ?: continue
            node.setAttribute("filename", relPath)

            findFailureLine(node, classname)?.let { line ->
                node.setAttribute("line", line)
            }
        }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        }
        destination.outputStream().use { out ->
            transformer.transform(DOMSource(doc), StreamResult(out))
        }
    }

    private fun findFailureLine(testcase: Element, classname: String): String? {
        val failure = firstChildElement(testcase, "failure")
            ?: firstChildElement(testcase, "error")
            ?: return null

        val stack = failure.textContent ?: return null

        return STACK_FRAME_REGEX
            .findAll(stack)
            .firstOrNull { it.groupValues[1] == classname }
            ?.groupValues
            ?.get(3)
    }

    private fun firstChildElement(parent: Element, tagName: String): Element? {
        val nodes = parent.getElementsByTagName(tagName)
        return if (nodes.length > 0) nodes.item(0) as? Element else null
    }

}