/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.exporter

import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import ru.alkoleft.context.platform.dto.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class MarkdownExporter(
    private val logic: ExporterLogic = BaseExporterLogic()
) : Exporter {

    @Throws(IOException::class)
    override fun writeProperties(context: PlatformGlobalContext, output: Path) {
        val markdown = StringBuilder()
        markdown.append("# Свойства\n\n")
        logic.extractProperties(context).use { properties ->
            appendProperties(markdown, properties.toList())
        }
        Files.writeString(output, markdown.toString())
    }

    @Throws(IOException::class)
    override fun writeMethods(context: PlatformGlobalContext, output: Path) {
        val markdown = StringBuilder()
        markdown.append("# Методы\n\n")
        logic.extractMethods(context).use { methods ->
            appendMethods(markdown, methods.toList(), "##")
        }
        Files.writeString(output, markdown.toString())
    }

    @Throws(IOException::class)
    override fun writeTypes(contexts: List<Context>, output: Path) {
        val markdown = StringBuilder()
        markdown.append("# Типы\n\n")

        logic.extractTypes(contexts).use { types ->
            types.sorted(compareBy { it.name }).forEach { type ->
                markdown.append("## ${type.name}\n\n")

                if (!type.constructors.isNullOrEmpty()) {
                    markdown.append("### Конструкторы\n\n")
                    type.constructors.forEach { constructor ->
                        appendConstructorDetails(markdown, constructor, type)
                    }
                }

                if (!type.properties.isNullOrEmpty()) {
                    markdown.append("### Свойства\n\n")
                    appendProperties(markdown, type.properties)
                }

                if (!type.methods.isNullOrEmpty()) {
                    markdown.append("### Методы\n\n")
                    appendMethods(markdown, type.methods, "####")
                }
            }
        }

        Files.writeString(output, markdown.toString())
    }

    private fun appendProperties(markdown: StringBuilder, properties: Collection<PropertyDefinition>) {
        if (properties.isEmpty()) {
            return
        }
        markdown.append("| Имя | Описание |\n")
        markdown.append("|---|---|\n")
        properties.sortedBy { it.name }.forEach { prop ->
            markdown.append(
                String.format(
                    "| `%s` | %s |\n",
                    escapeMarkdown(prop.name),
                    escapeMarkdown(prop.description)
                )
            )
        }
        markdown.append("\n")
    }

    private fun formatParametersForSignature(params: List<ParameterDefinition>?): String {
        if (params.isNullOrEmpty()) {
            return ""
        }
        return params.joinToString(", ") { param ->
            val paramType = if (!param.type.isNullOrEmpty()) param.type else "any"
            "${param.name}:$paramType"
        }
    }

    private fun formatCompleteSignature(method: MethodDefinition, sig: Signature): String {
        val paramsString = formatParametersForSignature(sig.params)
        val returnTypeString = if (method.returnType.isNotEmpty()) ":${method.returnType}" else ""
        return "${method.name}($paramsString)$returnTypeString"
    }

    private fun appendMethods(markdown: StringBuilder, methods: Collection<MethodDefinition>, headerLevel: String) {
        if (methods.isEmpty()) {
            return
        }
        methods.sortedBy { it.name }.forEach { method ->
            appendMethodDetails(markdown, method, headerLevel)
        }
    }

    private fun appendConstructorDetails(
        markdown: StringBuilder,
        constructor: ISignature,
        type: PlatformTypeDefinition
    ) {
        appendConstructorHeader(markdown, constructor)
        appendConstructorSignature(markdown, constructor, type)
        appendParameterTable(markdown, constructor.params)
    }

    private fun appendConstructorHeader(markdown: StringBuilder, constructor: ISignature) {
        markdown.append("#### ${constructor.name}\n\n")
        if (!constructor.description.isNullOrEmpty()) {
            markdown.append("${constructor.description}\n\n")
        }
    }

    private fun appendConstructorSignature(
        markdown: StringBuilder,
        constructor: ISignature,
        type: PlatformTypeDefinition
    ) {
        markdown.append("```bsl\n")
        markdown.append("Новый ${type.name}(${formatParametersForSignature(constructor.params)})\n")
        markdown.append("```\n\n")
    }

    private fun appendMethodDetails(markdown: StringBuilder, method: MethodDefinition, headerLevel: String) {
        appendMethodHeader(markdown, method, headerLevel)
        appendMethodSignaturesAndParameters(markdown, method)
    }

    private fun appendMethodHeader(markdown: StringBuilder, method: MethodDefinition, headerLevel: String) {
        markdown.append("$headerLevel ${method.name}\n\n")
        if (!method.description.isNullOrEmpty()) {
            markdown.append("${method.description}\n\n")
        }
    }

    private fun appendMethodSignaturesAndParameters(markdown: StringBuilder, method: MethodDefinition) {
        if (method.signature.isNotEmpty()) {
            method.signature.forEach { sig ->
                appendSignatureBlock(markdown, method, sig)
                appendParameterTable(markdown, sig.params)
            }
        } else {
            appendSignatureBlockForMethodWithoutParams(markdown, method)
        }
    }

    private fun appendSignatureBlock(markdown: StringBuilder, method: MethodDefinition, sig: Signature) {
        markdown.append("```bsl\n")
        markdown.append("${formatCompleteSignature(method, sig)}\n")
        markdown.append("```\n\n")
    }

    private fun appendSignatureBlockForMethodWithoutParams(markdown: StringBuilder, method: MethodDefinition) {
        markdown.append("```bsl\n")
        val returnTypeString = if (method.returnType.isNotEmpty()) ":${method.returnType}" else ""
        markdown.append("${method.name}()$returnTypeString")
        markdown.append("\n```\n\n")
    }

    private fun appendParameterTable(markdown: StringBuilder, params: List<ParameterDefinition>?) {
        if (params.isNullOrEmpty()) {
            return
        }

        markdown.append("**Параметры**\n\n")
        markdown.append("| Имя | Тип | Обязательность | Описание |\n")
        markdown.append("|---|---|---|---|\n")
        params.forEach { param ->
            markdown.append(
                String.format(
                    "| `%s` | `%s` | %s | %s |\n",
                    escapeMarkdown(param.name),
                    escapeMarkdown(param.type),
                    if (param.required) "Да" else "Нет",
                    escapeMarkdown(param.description)
                )
            )
        }
        markdown.append("\n")
    }

    private fun escapeMarkdown(text: String?): String {
        if (text == null) {
            return ""
        }
        return text.replace("\n", " ").replace("|", "\\|")
    }

    override fun getExtension(): String = ".md"
} 